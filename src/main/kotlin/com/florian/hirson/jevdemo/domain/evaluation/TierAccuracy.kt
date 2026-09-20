package com.florian.hirson.jevdemo.domain.evaluation

/**
 * How many of [total] labeled events in [tier] were classified with their
 * expected [com.florian.hirson.jevdemo.domain.triage.Category].
 */
data class TierAccuracy(val tier: ConfidenceTier, val correct: Int, val total: Int) {
    init {
        require(total >= 0) { "total ($total) must not be negative" }
        require(correct in 0..total) { "correct ($correct) must be between 0 and total ($total)" }
    }

    /** 0.0 when [total] is zero — an empty tier has no accuracy to report, not a failing one. */
    val accuracy: Double get() = if (total == 0) 0.0 else correct.toDouble() / total
}
