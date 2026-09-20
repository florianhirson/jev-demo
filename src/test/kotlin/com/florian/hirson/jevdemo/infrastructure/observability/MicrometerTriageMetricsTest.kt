package com.florian.hirson.jevdemo.infrastructure.observability

import com.florian.hirson.jevdemo.domain.triage.Actionable
import com.florian.hirson.jevdemo.domain.triage.Category
import com.florian.hirson.jevdemo.domain.triage.Classification
import com.florian.hirson.jevdemo.domain.triage.Confidence
import com.florian.hirson.jevdemo.domain.triage.RoutingDecision
import com.florian.hirson.jevdemo.domain.triage.Severity
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlin.test.Test
import kotlin.test.assertEquals

class MicrometerTriageMetricsTest {

    private val classification = Classification(
        category = Category.EXTERNAL_DEPENDENCY,
        categoryConfidence = Confidence(0.9),
        severity = Severity(0.8),
        severityConfidence = Confidence(0.9),
        actionable = Actionable(0.9),
    )

    @Test
    fun `recordRouted incremente un compteur tague par categorie et decision`() {
        val registry = SimpleMeterRegistry()
        val metrics = MicrometerTriageMetrics(registry)

        metrics.recordRouted(classification, RoutingDecision.AUTOMATIC)

        val counter = registry.get("triage.routed")
            .tag("category", "EXTERNAL_DEPENDENCY")
            .tag("decision", "AUTOMATIC")
            .counter()
        assertEquals(1.0, counter.count())
    }

    @Test
    fun `deux decisions differentes pour la meme categorie restent des compteurs distincts`() {
        val registry = SimpleMeterRegistry()
        val metrics = MicrometerTriageMetrics(registry)

        metrics.recordRouted(classification, RoutingDecision.AUTOMATIC)
        metrics.recordRouted(classification, RoutingDecision.FOR_REVIEW)
        metrics.recordRouted(classification, RoutingDecision.FOR_REVIEW)

        val automatic = registry.get("triage.routed").tag("decision", "AUTOMATIC").counter()
        val forReview = registry.get("triage.routed").tag("decision", "FOR_REVIEW").counter()
        assertEquals(1.0, automatic.count())
        assertEquals(2.0, forReview.count())
    }

    @Test
    fun `recordTriageFailed incremente un compteur d-echec dedie`() {
        val registry = SimpleMeterRegistry()
        val metrics = MicrometerTriageMetrics(registry)

        metrics.recordTriageFailed()
        metrics.recordTriageFailed()

        assertEquals(2.0, registry.get("triage.failed").counter().count())
    }
}
