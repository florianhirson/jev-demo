package com.florian.hirson.jevdemo.domain.triage

/**
 * Raw probability that a developer needs to act on a [LogEvent]. Mirrors
 * jev's `actionable` `noul` question directly: the graded probability is
 * kept rather than collapsed to a boolean, because the confidence-routing
 * policy (increment 4) needs the actual value, not a decision already taken
 * in the domain — so this exposes ordering, not a threshold: any cutoff is a
 * per-field policy choice for that increment to make, never one to hard-code
 * here.
 */
data class Actionable(val probability: Double) : Comparable<Actionable> {
    init {
        if (!probability.isFinite() || probability < 0.0 || probability > 1.0) {
            throw InvalidActionableProbability(probability)
        }
    }

    override fun compareTo(other: Actionable): Int = probability.compareTo(other.probability)

    /**
     * jev's `noul` carries no separate confidence field: the probability's
     * own distance from 0.5 is the certainty signal (0.5 = a coin flip, 0 or
     * 1 = as sure as jev gets). Derived, not stored, so it can never drift
     * out of sync with [probability].
     */
    val confidence: Confidence get() = Confidence(kotlin.math.abs(probability - 0.5) * 2)
}

/** An [Actionable] was rejected because a probability must be a finite number within 0.0..1.0. */
class InvalidActionableProbability(probability: Double) :
    IllegalArgumentException("Actionable probability must be a finite number within 0.0..1.0: $probability")
