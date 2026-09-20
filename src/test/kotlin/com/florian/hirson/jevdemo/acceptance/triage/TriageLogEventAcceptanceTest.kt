package com.florian.hirson.jevdemo.acceptance.triage

import com.florian.hirson.jevdemo.acceptance.triage.fakes.FakeLogClassifier
import com.florian.hirson.jevdemo.application.triage.usecase.TriageLogEventUseCase
import com.florian.hirson.jevdemo.domain.triage.Actionable
import com.florian.hirson.jevdemo.domain.triage.Category
import com.florian.hirson.jevdemo.domain.triage.Classification
import com.florian.hirson.jevdemo.domain.triage.Confidence
import com.florian.hirson.jevdemo.domain.triage.LogEvent
import com.florian.hirson.jevdemo.domain.triage.ReviewCase
import com.florian.hirson.jevdemo.domain.triage.RoutingDecision
import com.florian.hirson.jevdemo.domain.triage.RoutingThresholds
import com.florian.hirson.jevdemo.domain.triage.Severity
import com.florian.hirson.jevdemo.infrastructure.review.InMemoryReviewQueue
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Acceptance test for the triage domain: enters through the use case, with
 * both ports replaced by in-memory test doubles — no Spring context
 * involved.
 */
class TriageLogEventAcceptanceTest {

    private val thresholds = RoutingThresholds(
        category = Confidence(0.7), severity = Confidence(0.7), actionable = Confidence(0.7),
    )

    @Test
    fun `un log ERROR reconnu par jev est classifie selon la reponse retournee`() {
        // Arrange — un événement de log applicatif et un faux classifieur jev
        // qui sait y associer une classification confiante
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
        val triageLogEvent = TriageLogEventUseCase(classifier, InMemoryReviewQueue(), thresholds)

        // Act — le log est soumis au use case de triage
        val outcome = triageLogEvent.execute(logEvent)

        // Assert — la classification obtenue est celle produite par jev
        assertEquals(expected, outcome.classification)
    }

    @Test
    fun `une classification peu confiante part en revue plutot que d-etre traitee automatiquement`() {
        // Arrange — jev répond avec une confiance de catégorie sous le seuil
        val logEvent = LogEvent(
            message = "Unexpected token in response",
            occurredAt = Instant.parse("2026-09-19T10:15:30Z"),
        )
        val ambiguous = Classification(
            category = Category.APPLICATION_BUG,
            categoryConfidence = Confidence(0.4),
            severity = Severity(0.5),
            severityConfidence = Confidence(0.9),
            actionable = Actionable(0.9),
        )
        val classifier = FakeLogClassifier(mapOf(logEvent to ambiguous))
        val reviewQueue = InMemoryReviewQueue()
        val triageLogEvent = TriageLogEventUseCase(classifier, reviewQueue, thresholds)

        // Act
        val outcome = triageLogEvent.execute(logEvent)

        // Assert — le cas attend une décision humaine
        assertEquals(RoutingDecision.FOR_REVIEW, outcome.decision)
        assertEquals(listOf(ReviewCase(logEvent, ambiguous)), reviewQueue.pending())
    }

    @Test
    fun `une classification confiante sur tous les champs n-est jamais mise en revue`() {
        val logEvent = LogEvent(
            message = "Connection refused calling payment-service",
            occurredAt = Instant.parse("2026-09-19T10:15:30Z"),
        )
        val confident = Classification(
            category = Category.EXTERNAL_DEPENDENCY,
            categoryConfidence = Confidence(0.95),
            severity = Severity(0.8),
            severityConfidence = Confidence(0.85),
            actionable = Actionable(0.9),
        )
        val classifier = FakeLogClassifier(mapOf(logEvent to confident))
        val reviewQueue = InMemoryReviewQueue()
        val triageLogEvent = TriageLogEventUseCase(classifier, reviewQueue, thresholds)

        val outcome = triageLogEvent.execute(logEvent)

        assertEquals(RoutingDecision.AUTOMATIC, outcome.decision)
        assertEquals(emptyList(), reviewQueue.pending())
    }
}
