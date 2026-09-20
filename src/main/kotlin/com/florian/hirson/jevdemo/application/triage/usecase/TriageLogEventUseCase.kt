package com.florian.hirson.jevdemo.application.triage.usecase

import com.florian.hirson.jevdemo.domain.triage.Classification
import com.florian.hirson.jevdemo.domain.triage.LogClassifier
import com.florian.hirson.jevdemo.domain.triage.LogEvent
import com.florian.hirson.jevdemo.domain.triage.ReviewCase
import com.florian.hirson.jevdemo.domain.triage.ReviewQueue
import com.florian.hirson.jevdemo.domain.triage.RoutingDecision
import com.florian.hirson.jevdemo.domain.triage.RoutingThresholds
import com.florian.hirson.jevdemo.domain.triage.TriageMetrics

/**
 * Orchestrates the [LogClassifier] port to triage a single [LogEvent], then
 * routes the result by confidence (see [Classification.route]): a
 * [RoutingDecision.FOR_REVIEW] classification is handed to [reviewQueue]
 * rather than acted on automatically. The [TriageOutcome] returned carries
 * that routing decision, not just the classification, so a caller can tell
 * which happened. Every routed outcome is recorded via [metrics], regardless
 * of the decision. The insertion point for whatever acts on an
 * [RoutingDecision.AUTOMATIC] classification in a later increment.
 */
class TriageLogEventUseCase(
    private val classifier: LogClassifier,
    private val reviewQueue: ReviewQueue,
    private val thresholds: RoutingThresholds,
    private val metrics: TriageMetrics,
) {

    fun execute(logEvent: LogEvent): TriageOutcome {
        val classification = classifier.classify(logEvent)
        val decision = classification.route(thresholds)
        metrics.recordRouted(classification, decision)
        if (decision == RoutingDecision.FOR_REVIEW) {
            reviewQueue.submit(ReviewCase(logEvent, classification))
        }
        return TriageOutcome(classification, decision)
    }
}
