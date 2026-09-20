package com.florian.hirson.jevdemo.acceptance.triage

import com.florian.hirson.jevdemo.acceptance.triage.fakes.FakeLogClassifier
import com.florian.hirson.jevdemo.application.triage.usecase.ClassifyLogEventUseCase
import com.florian.hirson.jevdemo.domain.triage.Actionable
import com.florian.hirson.jevdemo.domain.triage.Category
import com.florian.hirson.jevdemo.domain.triage.Classification
import com.florian.hirson.jevdemo.domain.triage.Confidence
import com.florian.hirson.jevdemo.domain.triage.LogEvent
import com.florian.hirson.jevdemo.domain.triage.Severity
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Acceptance test for the triage domain: enters through the use case, with a
 * port replaced by an in-memory test double — no Spring context involved.
 */
class ClassifyLogEventAcceptanceTest {

    @Test
    fun `un log ERROR reconnu par jev est classifie selon la reponse retournee`() {
        // Arrange — un événement de log applicatif et un faux classifieur jev
        // qui sait y associer une classification
        val logEvent = LogEvent(
            message = "Connection refused calling payment-service",
            stackTrace = "java.net.ConnectException: Connection refused",
            occurredAt = Instant.parse("2026-09-19T10:15:30Z"),
        )
        val expected = Classification(
            category = Category.EXTERNAL_DEPENDENCY,
            categoryConfidence = Confidence(0.95),
            severity = Severity(0.8),
            severityConfidence = Confidence(0.85),
            actionable = Actionable(0.9),
        )
        val classifier = FakeLogClassifier(mapOf(logEvent to expected))
        val classifyLogEvent = ClassifyLogEventUseCase(classifier)

        // Act — le log est soumis au use case de triage
        val classification = classifyLogEvent.execute(logEvent)

        // Assert — la classification obtenue est celle produite par jev
        assertEquals(expected, classification)
    }
}
