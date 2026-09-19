package com.florian.hirson.jevdemo.domain.triage

/**
 * Position on a severity spectrum. Mirrors jev's `severity` `score` question:
 * a weighted position across an ordered set of levels. The exact number and
 * labels of those levels are decided when the real jev prompt is written
 * (increment 3), so only the lower bound is enforced here — but a value that
 * isn't a real number is never a valid position, regardless of that bound.
 */
data class Severity(val value: Double) : Comparable<Severity> {
    init {
        if (!value.isFinite() || value < 0.0) throw InvalidSeverity(value)
    }

    override fun compareTo(other: Severity): Int = value.compareTo(other.value)
}

/** A [Severity] was rejected because a position on the spectrum must be a non-negative, finite number. */
class InvalidSeverity(value: Double) :
    IllegalArgumentException("Severity must be a non-negative, finite number: $value")
