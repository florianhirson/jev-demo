package com.florian.hirson.jevdemo.domain.triage

/**
 * The minimum [Confidence] required on each field of a [Classification]
 * before it's trusted enough to handle automatically. One threshold per
 * field, never a single global one — per jev-1.13's documented jaggedness,
 * a threshold calibrated on one answer type doesn't transfer to another.
 */
data class RoutingThresholds(
    val categoryConfidence: Confidence,
    val severityConfidence: Confidence,
    val actionableConfidence: Confidence,
)
