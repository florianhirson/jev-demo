package com.florian.hirson.jevdemo.domain.triage

/**
 * Position on a severity spectrum. Mirrors jev's `severity` `score` question:
 * a weighted position across an ordered set of levels. The exact number and
 * labels of those levels are decided when the real jev prompt is written
 * (increment 3), so only the lower bound is enforced here.
 */
data class Severity(val value: Double) {
    init {
        if (value < 0.0) throw NegativeSeverity(value)
    }
}

/** A [Severity] was rejected because a position on the spectrum cannot be negative. */
class NegativeSeverity(value: Double) :
    IllegalArgumentException("Severity must not be negative: $value")
