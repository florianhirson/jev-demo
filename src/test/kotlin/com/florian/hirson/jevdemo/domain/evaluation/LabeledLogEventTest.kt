package com.florian.hirson.jevdemo.domain.evaluation

import com.florian.hirson.jevdemo.domain.triage.Actionable
import com.florian.hirson.jevdemo.domain.triage.Category
import com.florian.hirson.jevdemo.domain.triage.Classification
import com.florian.hirson.jevdemo.domain.triage.Confidence
import com.florian.hirson.jevdemo.domain.triage.LogEvent
import com.florian.hirson.jevdemo.domain.triage.Severity
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LabeledLogEventTest {

    private val logEvent = LogEvent(message = "Connection refused calling payment-service", occurredAt = Instant.parse("2026-09-20T10:00:00Z"))

    private fun classification(category: Category) = Classification(
        category = category,
        categoryConfidence = Confidence(0.9),
        severity = Severity(0.5),
        severityConfidence = Confidence(0.9),
        actionable = Actionable(0.8),
    )

    @Test
    fun `une classification qui partage la categorie attendue est correcte`() {
        val labeled = LabeledLogEvent(logEvent, expectedCategory = Category.EXTERNAL_DEPENDENCY)

        assertTrue(labeled.isCorrectlyClassifiedBy(classification(Category.EXTERNAL_DEPENDENCY)))
    }

    @Test
    fun `une classification d-une autre categorie n-est pas correcte`() {
        val labeled = LabeledLogEvent(logEvent, expectedCategory = Category.EXTERNAL_DEPENDENCY)

        assertFalse(labeled.isCorrectlyClassifiedBy(classification(Category.APPLICATION_BUG)))
    }
}
