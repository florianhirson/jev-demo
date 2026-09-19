package com.florian.hirson.jevdemo.infrastructure.classification.jev

import com.florian.hirson.jevdemo.domain.triage.Actionable
import com.florian.hirson.jevdemo.domain.triage.Category
import com.florian.hirson.jevdemo.domain.triage.Classification
import com.florian.hirson.jevdemo.domain.triage.LogClassifier
import com.florian.hirson.jevdemo.domain.triage.LogEvent
import com.florian.hirson.jevdemo.domain.triage.RedactSensitiveData
import com.florian.hirson.jevdemo.domain.triage.Severity
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.retry.Retry
import org.springframework.web.client.RestClientResponseException

/**
 * The real jev adapter: [LogClassifier] implemented by calling jev's System
 * One API. Every occurrence of jev's documented model jaggedness this
 * project accounts for lives here — see CLAUDE.md.
 */
class JevLogClassifier(
    private val client: SystemOneClient,
    private val properties: JevProperties,
    private val circuitBreaker: CircuitBreaker,
    private val retry: Retry,
) : LogClassifier {

    override fun classify(logEvent: LogEvent): Classification {
        val request = buildRequest(logEvent)
        val call = CircuitBreaker.decorateSupplier(circuitBreaker, Retry.decorateSupplier(retry) { callClient(request) })
        return toClassification(call.get())
    }

    private fun callClient(request: SystemOneRequest): SystemOneResponse = try {
        client.classify(request)
    } catch (exception: RestClientResponseException) {
        throw mapException(exception)
    }

    private fun mapException(exception: RestClientResponseException): JevApiException = when (exception.statusCode.value()) {
        401 -> JevUnauthorized(exception)
        422 -> JevInvalidRequest(exception)
        429 -> JevRateLimited(exception)
        529 -> JevOverloaded(exception)
        else -> JevUnavailable(exception)
    }

    private fun buildRequest(logEvent: LogEvent): SystemOneRequest {
        val redactedMessage = RedactSensitiveData.execute(logEvent.message)
        val redactedStackTrace = logEvent.stackTrace
            ?.let(RedactSensitiveData::execute)
            ?.take(MAX_STACK_TRACE_LENGTH)
        val state = listOfNotNull(redactedMessage, redactedStackTrace).joinToString("\n\n")

        return SystemOneRequest(
            state = state,
            model = properties.model,
            questions = mapOf(
                "category" to Question.Choice(
                    instructions = "Which category best explains why this application error occurred?",
                    criteria = mapOf(
                        "application_bug" to "A defect in this application's own code: a null pointer, a logic error, an unhandled edge case.",
                        "external_dependency" to "A failure calling another service, database, or third-party API this application depends on.",
                        "configuration" to "Missing or incorrect configuration: a bad environment variable, a missing credential, a misconfigured connection.",
                        "noise" to "Not a real problem: expected behavior, a benign warning logged at ERROR level, or a duplicate of another error.",
                    ),
                ),
                "severity" to Question.Score(
                    instructions = "How severe is this error for the application's users and operators?",
                    criteria = listOf(
                        "Cosmetic or already recovered; no user-visible impact.",
                        "Degraded experience for some users; no data loss.",
                        "A feature is broken or unavailable for users.",
                        "Critical: data loss, security impact, or the application is down.",
                    ),
                ),
                "actionable" to Question.Noul(
                    instructions = "Does a developer need to take action on this error?",
                    criteria = NoulCriteria(
                        whenTrue = "A human should investigate or fix something.",
                        whenFalse = "No action needed: expected, already handled, or not a real problem.",
                    ),
                ),
            ),
        )
    }

    private fun toClassification(response: SystemOneResponse): Classification {
        val category = (response.answers.getValue("category") as Answer.Choice).let { toCategory(it.choice) }
        val severity = (response.answers.getValue("severity") as Answer.Score).let { Severity(it.score) }
        val actionable = (response.answers.getValue("actionable") as Answer.Noul).let { Actionable(it.noul) }
        return Classification(category, severity, actionable)
    }

    private fun toCategory(choice: String): Category = when (choice) {
        "application_bug" -> Category.APPLICATION_BUG
        "external_dependency" -> Category.EXTERNAL_DEPENDENCY
        "configuration" -> Category.CONFIGURATION
        "noise" -> Category.NOISE
        else -> throw JevUnavailable(IllegalStateException("Unknown category choice from jev: $choice"))
    }

    private companion object {
        const val MAX_STACK_TRACE_LENGTH = 2000
    }
}
