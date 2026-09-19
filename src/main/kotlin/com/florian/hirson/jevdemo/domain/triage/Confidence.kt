package com.florian.hirson.jevdemo.domain.triage

/**
 * How certain jev is about one answer — a `choice`'s or a `score`'s
 * `confidence` field. Per jev-1.13's documented jaggedness, confidence isn't
 * comparable across answer types (a `noul` carries none at all; see
 * [Actionable.confidence] for how that one is derived instead), so a
 * [RoutingThresholds] is always one [Confidence] per field, never a single
 * global number.
 */
data class Confidence(val value: Double) : Comparable<Confidence> {
    init {
        if (!value.isFinite() || value < 0.0 || value > 1.0) throw InvalidConfidence(value)
    }

    override fun compareTo(other: Confidence): Int = value.compareTo(other.value)
}

/** A [Confidence] was rejected because it must be a finite number within 0.0..1.0. */
class InvalidConfidence(value: Double) :
    IllegalArgumentException("Confidence must be a finite number within 0.0..1.0: $value")
