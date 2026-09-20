package com.florian.hirson.jevdemo.domain.triage

/** Port: observing what happens during triage, for a metrics adapter to expose. */
interface TriageMetrics {
    fun recordRouted(classification: Classification, decision: RoutingDecision)
    fun recordClassificationFailed()
}
