package com.florian.hirson.jevdemo.infrastructure.classification.jev

import com.florian.hirson.jevdemo.domain.triage.Actionable
import com.florian.hirson.jevdemo.domain.triage.Category
import com.florian.hirson.jevdemo.domain.triage.LogEvent
import com.florian.hirson.jevdemo.domain.triage.Severity
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import org.springframework.web.client.ResourceAccessException
import java.io.IOException
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class JevLogClassifierTest {

    private class FakeSystemOneClient(
        private val behavior: (SystemOneRequest) -> SystemOneResponse,
    ) : SystemOneClient {
        val requests = CopyOnWriteArrayList<SystemOneRequest>()

        override fun classify(request: SystemOneRequest): SystemOneResponse {
            requests.add(request)
            return behavior(request)
        }
    }

    private fun logEvent(message: String, stackTrace: String? = null) =
        LogEvent(message = message, stackTrace = stackTrace, occurredAt = Instant.parse("2026-09-19T10:15:30Z"))

    private fun response() = SystemOneResponse(
        model = "jev-latest",
        answers = mapOf(
            "category" to Answer.Choice("external_dependency", 0.9, mapOf("external_dependency" to 0.9)),
            "severity" to Answer.Score(2.0, mapOf("0" to "low", "1" to "medium", "2" to "high"), 0.7, mapOf("2" to 0.7)),
            "actionable" to Answer.Noul(0.85),
        ),
        usage = Usage(100, 20),
    )

    private fun newClassifier(
        client: SystemOneClient,
        maxAttempts: Int = 3,
    ): JevLogClassifier {
        val circuitBreaker = CircuitBreaker.of(
            "jev-test",
            CircuitBreakerConfig.custom().minimumNumberOfCalls(1000).build(),
        )
        val retry = Retry.of(
            "jev-test",
            RetryConfig.custom<Any>()
                .maxAttempts(maxAttempts)
                .waitDuration(Duration.ofMillis(1))
                .retryOnException { it is JevApiException && it.isTransient() }
                .build(),
        )
        return JevLogClassifier(client, JevProperties(model = "jev-latest"), circuitBreaker, retry)
    }

    @Test
    fun `une reponse jev valide est mappee vers une Classification`() {
        val client = FakeSystemOneClient { response() }
        val classifier = newClassifier(client)

        val classification = classifier.classify(logEvent("Connection refused calling payment-service"))

        assertEquals(Category.EXTERNAL_DEPENDENCY, classification.category)
        assertEquals(Severity(2.0), classification.severity)
        assertEquals(Actionable(0.85), classification.actionable)
    }

    @Test
    fun `la requete envoyee a jev porte l-etat redige et les trois questions`() {
        val client = FakeSystemOneClient { response() }
        val classifier = newClassifier(client)

        classifier.classify(logEvent("Timeout calling 10.0.0.1", stackTrace = "at Foo.bar(Foo.java:1)"))

        val request = client.requests.single()
        assertTrue(request.state.contains("[IP]"))
        assertTrue(request.state.contains("Foo.bar"))
        assertEquals(setOf("category", "severity", "actionable"), request.questions.keys)
        assertTrue(request.questions["category"] is Question.Choice)
        assertTrue(request.questions["severity"] is Question.Score)
        assertTrue(request.questions["actionable"] is Question.Noul)
    }

    @Test
    fun `une erreur 401 n-est jamais retentee`() {
        var callCount = 0
        val client = FakeSystemOneClient {
            callCount++
            throw JevUnauthorized()
        }
        val classifier = newClassifier(client)

        assertFailsWith<JevUnauthorized> { classifier.classify(logEvent("boom")) }
        assertEquals(1, callCount)
    }

    @Test
    fun `une erreur 429 est retentee puis reussit`() {
        var callCount = 0
        val client = FakeSystemOneClient {
            callCount++
            if (callCount < 3) throw JevRateLimited() else response()
        }
        val classifier = newClassifier(client, maxAttempts = 3)

        val classification = classifier.classify(logEvent("boom"))

        assertEquals(3, callCount)
        assertEquals(Category.EXTERNAL_DEPENDENCY, classification.category)
    }

    @Test
    fun `une erreur 529 persistante epuise les tentatives puis echoue`() {
        var callCount = 0
        val client = FakeSystemOneClient {
            callCount++
            throw JevOverloaded()
        }
        val classifier = newClassifier(client, maxAttempts = 3)

        assertFailsWith<JevOverloaded> { classifier.classify(logEvent("boom")) }
        assertEquals(3, callCount)
    }

    @Test
    fun `une erreur reseau est mappee vers JevNetworkError et retentee`() {
        var callCount = 0
        val client = object : SystemOneClient {
            override fun classify(request: SystemOneRequest): SystemOneResponse {
                callCount++
                if (callCount < 2) throw ResourceAccessException("connection refused", IOException("boom"))
                return response()
            }
        }
        val classifier = newClassifier(client, maxAttempts = 3)

        val classification = classifier.classify(logEvent("boom"))

        assertEquals(2, callCount)
        assertEquals(Category.EXTERNAL_DEPENDENCY, classification.category)
    }

    @Test
    fun `une reponse jev incomplete est signalee comme JevUnavailable plutot que de fuiter une exception technique`() {
        val incomplete = SystemOneResponse(
            model = "jev-latest",
            answers = mapOf(
                "category" to Answer.Choice("external_dependency", 0.9, mapOf("external_dependency" to 0.9)),
                // "severity" manquant
                "actionable" to Answer.Noul(0.85),
            ),
            usage = Usage(100, 20),
        )
        val client = FakeSystemOneClient { incomplete }
        val classifier = newClassifier(client)

        assertFailsWith<JevUnavailable> { classifier.classify(logEvent("boom")) }
    }
}
