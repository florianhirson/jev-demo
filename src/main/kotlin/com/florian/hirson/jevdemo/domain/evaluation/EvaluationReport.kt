package com.florian.hirson.jevdemo.domain.evaluation

/**
 * Category-classification accuracy, broken down by [ConfidenceTier] — always
 * one [TierAccuracy] per tier, so an empty tier is still visible in the
 * report rather than silently missing.
 */
data class EvaluationReport(val tierAccuracies: List<TierAccuracy>) {
    init {
        val tiers = tierAccuracies.map { it.tier }
        require(tiers.size == ConfidenceTier.entries.size && tiers.toSet() == ConfidenceTier.entries.toSet()) {
            "tierAccuracies must cover every ConfidenceTier exactly once: $tiers"
        }
    }

    companion object {
        /** Groups one (tier, wasCorrect) pair per classified event into a [TierAccuracy] per [ConfidenceTier]. */
        fun of(results: List<Pair<ConfidenceTier, Boolean>>): EvaluationReport {
            val outcomesByTier = results.groupBy({ it.first }, { it.second })
            val tierAccuracies = ConfidenceTier.entries.map { tier ->
                val outcomes = outcomesByTier[tier].orEmpty()
                TierAccuracy(tier, correct = outcomes.count { it }, total = outcomes.size)
            }
            return EvaluationReport(tierAccuracies)
        }
    }
}
