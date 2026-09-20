package com.florian.hirson.jevdemo.infrastructure.observability

import com.florian.hirson.jevdemo.domain.triage.Classification
import com.florian.hirson.jevdemo.domain.triage.RoutingDecision
import com.florian.hirson.jevdemo.domain.triage.TriageMetrics
import io.micrometer.core.instrument.MeterRegistry

/**
 * [TriageMetrics] backed by Micrometer, exposed to Prometheus via Actuator.
 * `category` and `decision` are both low-cardinality closed sets (four
 * [com.florian.hirson.jevdemo.domain.triage.Category] values, two
 * [RoutingDecision] values), so tagging by them stays safe — never tag a
 * metric with anything unbounded like a message or an id.
 */
class MicrometerTriageMetrics(private val registry: MeterRegistry) : TriageMetrics {

    override fun recordRouted(classification: Classification, decision: RoutingDecision) {
        registry.counter(
            "triage.routed",
            "category", classification.category.name,
            "decision", decision.name,
        ).increment()
    }

    override fun recordTriageFailed() {
        registry.counter("triage.failed").increment()
    }
}
