package com.florian.hirson.jevdemo.infrastructure.classification

import com.florian.hirson.jevdemo.domain.triage.Actionable
import com.florian.hirson.jevdemo.domain.triage.Category
import com.florian.hirson.jevdemo.domain.triage.Classification
import com.florian.hirson.jevdemo.domain.triage.LogClassifier
import com.florian.hirson.jevdemo.domain.triage.LogEvent
import com.florian.hirson.jevdemo.domain.triage.Severity
import com.florian.hirson.jevdemo.infrastructure.cache.InMemoryClassificationCache
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class CachingLogClassifierTest {

    private class CountingLogClassifier(private val classification: Classification) : LogClassifier {
        var callCount = 0
            private set

        override fun classify(logEvent: LogEvent): Classification {
            callCount++
            return classification
        }
    }

    private fun logEvent(message: String) =
        LogEvent(message = message, occurredAt = Instant.parse("2026-09-19T10:15:30Z"))

    @Test
    fun `un cache miss appelle le delegate et memorise le resultat`() {
        val expected = Classification(Category.EXTERNAL_DEPENDENCY, Severity(0.8), Actionable(0.9))
        val delegate = CountingLogClassifier(expected)
        val classifier = CachingLogClassifier(delegate, InMemoryClassificationCache())

        val result = classifier.classify(logEvent("Connection refused calling payment-service"))

        assertEquals(expected, result)
        assertEquals(1, delegate.callCount)
    }

    @Test
    fun `un cache hit n-appelle pas le delegate`() {
        val expected = Classification(Category.EXTERNAL_DEPENDENCY, Severity(0.8), Actionable(0.9))
        val delegate = CountingLogClassifier(expected)
        val classifier = CachingLogClassifier(delegate, InMemoryClassificationCache())
        classifier.classify(logEvent("Connection refused calling payment-service"))

        val result = classifier.classify(logEvent("Connection refused calling payment-service"))

        assertEquals(expected, result)
        assertEquals(1, delegate.callCount)
    }

    @Test
    fun `deux messages differant seulement par une donnee sensible partagent le cache`() {
        val expected = Classification(Category.EXTERNAL_DEPENDENCY, Severity(0.8), Actionable(0.9))
        val delegate = CountingLogClassifier(expected)
        val classifier = CachingLogClassifier(delegate, InMemoryClassificationCache())
        classifier.classify(logEvent("Timeout calling 10.0.0.1 for user alice@acme.example"))

        val result = classifier.classify(logEvent("Timeout calling 10.0.0.2 for user bob@acme.example"))

        assertEquals(expected, result)
        assertEquals(1, delegate.callCount)
    }
}
