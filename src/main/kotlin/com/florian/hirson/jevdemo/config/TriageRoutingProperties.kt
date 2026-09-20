package com.florian.hirson.jevdemo.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Per-field minimum confidence for automatic routing (see
 * [com.florian.hirson.jevdemo.domain.triage.RoutingThresholds]) — one knob
 * per jev answer type, never a single global threshold. 0.7 on all three is
 * a starting guess for a demo, not a calibrated value; see increment 6 for
 * calibration against the test dataset.
 */
@ConfigurationProperties(prefix = "triage.routing")
data class TriageRoutingProperties(
    val categoryThreshold: Double = 0.7,
    val severityThreshold: Double = 0.7,
    val actionableThreshold: Double = 0.7,
)
