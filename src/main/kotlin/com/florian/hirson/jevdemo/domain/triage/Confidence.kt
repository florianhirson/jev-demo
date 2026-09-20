package com.florian.hirson.jevdemo.domain.triage

/**
 * How certain jev is about one answer, normalized to 0.0..1.0 — a `choice`'s
 * or a `score`'s own `confidence` field, or (see [Actionable.confidence]) the
 * certainty derived from a `noul`'s distance to 0.5. All three answer types
 * share this scale, but per jev-1.13's documented jaggedness a value's
 * calibration doesn't transfer between answer types, so a
 * [RoutingThresholds] always sets one [Confidence] per field, never a single
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
