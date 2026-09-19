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
        if (probability < 0.0 || probability > 1.0) throw ActionableProbabilityOutOfRange(probability)
    }
}

/** An [Actionable] was rejected because a probability must lie within 0.0..1.0. */
class ActionableProbabilityOutOfRange(probability: Double) :
    IllegalArgumentException("Actionable probability must be within 0.0..1.0: $probability")
