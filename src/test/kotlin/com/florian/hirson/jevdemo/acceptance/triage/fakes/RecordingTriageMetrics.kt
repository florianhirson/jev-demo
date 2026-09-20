package com.florian.hirson.jevdemo.acceptance.triage.fakes

import com.florian.hirson.jevdemo.domain.triage.Classification
import com.florian.hirson.jevdemo.domain.triage.RoutingDecision
import com.florian.hirson.jevdemo.domain.triage.TriageMetrics
import java.util.concurrent.CopyOnWriteArrayList

/** In-memory [TriageMetrics] test double, recording calls for assertion. */
class RecordingTriageMetrics : TriageMetrics {

    val routed = CopyOnWriteArrayList<Pair<Classification, RoutingDecision>>()
    var classificationFailures = 0
        private set

    override fun recordRouted(classification: Classification, decision: RoutingDecision) {
        routed.add(classification to decision)
    }

    override fun recordClassificationFailed() {
        classificationFailures++
    }
}
