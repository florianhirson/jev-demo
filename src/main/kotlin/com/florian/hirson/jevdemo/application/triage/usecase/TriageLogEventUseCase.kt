package com.florian.hirson.jevdemo.application.triage.usecase

import com.florian.hirson.jevdemo.domain.triage.Classification
import com.florian.hirson.jevdemo.domain.triage.LogClassifier
import com.florian.hirson.jevdemo.domain.triage.LogEvent
import com.florian.hirson.jevdemo.domain.triage.ReviewCase
import com.florian.hirson.jevdemo.domain.triage.ReviewQueue
import com.florian.hirson.jevdemo.domain.triage.RoutingDecision
import com.florian.hirson.jevdemo.domain.triage.RoutingThresholds

/**
 * Orchestrates the [LogClassifier] port to triage a single [LogEvent], then
 * routes the result by confidence (see [Classification.route]): a
 * [RoutingDecision.FOR_REVIEW] classification is handed to [reviewQueue]
 * rather than acted on automatically. The insertion point for whatever acts
 * on an [RoutingDecision.AUTOMATIC] classification in a later increment.
 */
class TriageLogEventUseCase(
    private val classifier: LogClassifier,
    private val reviewQueue: ReviewQueue,
    private val thresholds: RoutingThresholds,
) {

    fun execute(logEvent: LogEvent): Classification {
        val classification = classifier.classify(logEvent)
        if (classification.route(thresholds) == RoutingDecision.FOR_REVIEW) {
            reviewQueue.enqueue(ReviewCase(logEvent, classification))
        }
        return classification
    }
}
