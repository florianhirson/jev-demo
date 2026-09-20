package com.florian.hirson.jevdemo.application.evaluation

import com.florian.hirson.jevdemo.domain.evaluation.ConfidenceTier
import com.florian.hirson.jevdemo.domain.evaluation.ErrorsDataset
import com.florian.hirson.jevdemo.domain.evaluation.EvaluationReport
import com.florian.hirson.jevdemo.domain.triage.LogClassifier

/**
 * Classifies every labeled event in [dataset] with [classifier] and reports
 * category accuracy per [ConfidenceTier] — checking whether jev's own
 * confidence actually predicts correctness, the assumption
 * [com.florian.hirson.jevdemo.domain.triage.RoutingThresholds] relies on.
 */
class EvaluateClassificationAccuracyUseCase(
    private val dataset: ErrorsDataset,
    private val classifier: LogClassifier,
) {

    fun execute(): EvaluationReport {
        val results = dataset.load().map { labeled ->
            val classification = classifier.classify(labeled.logEvent)
            ConfidenceTier.of(classification.categoryConfidence) to (classification.category == labeled.expectedCategory)
        }
        return EvaluationReport.of(results)
    }
}
