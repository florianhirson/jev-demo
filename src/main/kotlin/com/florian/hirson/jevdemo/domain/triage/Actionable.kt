package com.florian.hirson.jevdemo.domain.triage

/**
 * Raw probability that a developer needs to act on a [LogEvent]. Mirrors
 * jev's `actionable` `noul` question directly: the graded probability is
 * kept rather than collapsed to a boolean, because the confidence-routing
 * policy (increment 4) needs the actual value, not a decision already taken
 * in the domain.
 */
data class Actionable(val probability: Double) {
    init {
        if (!probability.isFinite() || probability < 0.0 || probability > 1.0) {
            throw InvalidActionableProbability(probability)
        }
    }

    /** A convenience reading, not a routing decision: the policy in increment 4 uses [probability] directly. */
    val isLikelyActionable: Boolean get() = probability > 0.5
}

/** An [Actionable] was rejected because a probability must be a finite number within 0.0..1.0. */
class InvalidActionableProbability(probability: Double) :
    IllegalArgumentException("Actionable probability must be a finite number within 0.0..1.0: $probability")
