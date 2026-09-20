package com.florian.hirson.jevdemo.domain.evaluation

import com.florian.hirson.jevdemo.domain.triage.Confidence

/**
 * A bracket of [Confidence], used to check whether jev's category accuracy
 * actually rises with its own confidence — the assumption
 * [com.florian.hirson.jevdemo.domain.triage.RoutingThresholds] (increment 4)
 * relies on to trust an [com.florian.hirson.jevdemo.domain.triage.RoutingDecision.AUTOMATIC]
 * classification. Boundaries are half-open except the top one: `[0.0, 0.5)`,
 * `[0.5, 0.7)`, `[0.7, 0.9)`, `[0.9, 1.0]`.
 */
enum class ConfidenceTier {
    LOW,
    MEDIUM,
    HIGH,
    VERY_HIGH,
    ;

    companion object {
        fun of(confidence: Confidence): ConfidenceTier = when {
            confidence.value < 0.5 -> LOW
            confidence.value < 0.7 -> MEDIUM
            confidence.value < 0.9 -> HIGH
            else -> VERY_HIGH
        }
    }
}
