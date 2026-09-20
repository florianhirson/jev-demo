package com.florian.hirson.jevdemo.domain.evaluation

/**
 * How many of [total] labeled events in [tier] were classified with their
 * expected [com.florian.hirson.jevdemo.domain.triage.Category].
 */
data class TierAccuracy(val tier: ConfidenceTier, val correct: Int, val total: Int) {
    init {
        if (total < 0 || correct !in 0..total) throw InvalidTierAccuracy(tier, correct, total)
    }

    /**
     * Null when [total] is zero: an empty tier has no accuracy to report,
     * which is a different fact from "every observation in this tier was
     * wrong" (accuracy 0.0) — collapsing the two into the same 0.0 would
     * hide that distinction from whoever reads the report.
     */
    val accuracy: Double? get() = if (total == 0) null else correct.toDouble() / total
}

/** A [TierAccuracy] was rejected because [correct] must be between 0 and a non-negative [total]. */
class InvalidTierAccuracy(tier: ConfidenceTier, correct: Int, total: Int) :
    IllegalArgumentException("TierAccuracy for $tier must have correct in 0..total, with total >= 0: correct=$correct, total=$total")
