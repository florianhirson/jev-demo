package com.florian.hirson.jevdemo.domain.evaluation

/**
 * Category-classification accuracy, broken down by [ConfidenceTier] — always
 * one [TierAccuracy] per tier, so an empty tier is still visible in the
 * report rather than silently missing.
 */
data class EvaluationReport(val tierAccuracies: List<TierAccuracy>) {
    init {
        val tiers = tierAccuracies.map { it.tier }
        if (tiers.size != ConfidenceTier.entries.size || tiers.toSet() != ConfidenceTier.entries.toSet()) {
            throw IncompleteEvaluationReport(tiers)
        }
    }

    companion object {
        /** Groups one [TierOutcome] per classified event into a [TierAccuracy] per [ConfidenceTier]. */
        fun of(outcomes: List<TierOutcome>): EvaluationReport {
            val outcomesByTier = outcomes.groupBy { it.tier }
            val tierAccuracies = ConfidenceTier.entries.map { tier ->
                val forTier = outcomesByTier[tier].orEmpty()
                TierAccuracy(tier, correct = forTier.count { it.correct }, total = forTier.size)
            }
            return EvaluationReport(tierAccuracies)
        }
    }
}

/** An [EvaluationReport] was rejected because [EvaluationReport.tierAccuracies] must cover every [ConfidenceTier] exactly once. */
class IncompleteEvaluationReport(tiers: List<ConfidenceTier>) :
    IllegalArgumentException("tierAccuracies must cover every ConfidenceTier exactly once: $tiers")
