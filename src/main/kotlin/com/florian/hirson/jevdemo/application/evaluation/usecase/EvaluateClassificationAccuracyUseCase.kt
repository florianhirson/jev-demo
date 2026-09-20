package com.florian.hirson.jevdemo.application.evaluation.usecase

import com.florian.hirson.jevdemo.domain.evaluation.ConfidenceTier
import com.florian.hirson.jevdemo.domain.evaluation.EvaluationDataset
import com.florian.hirson.jevdemo.domain.evaluation.EvaluationReport
import com.florian.hirson.jevdemo.domain.evaluation.TierOutcome
import com.florian.hirson.jevdemo.domain.triage.LogClassifier

/**
 * Classifies every labeled event in [dataset] with [classifier] and reports
 * category accuracy per [ConfidenceTier] — checking whether jev's own
 * confidence actually predicts correctness, the assumption
 * [com.florian.hirson.jevdemo.domain.triage.RoutingThresholds] relies on.
 */
class EvaluateClassificationAccuracyUseCase(
    private val dataset: EvaluationDataset,
    private val classifier: LogClassifier,
) {

    fun execute(): EvaluationReport {
        val outcomes = dataset.labeledLogEvents().map { labeled ->
            val classification = classifier.classify(labeled.logEvent)
            TierOutcome(ConfidenceTier.of(classification.categoryConfidence), labeled.isCorrectlyClassifiedBy(classification))
        }
        return EvaluationReport.of(outcomes)
    }
}
