package com.florian.hirson.jevdemo.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Tunables for the demo log simulator (increment 6): replays `data/errors.jsonl`
 * as real ERROR logs so the ingestion pipeline (increment 2) has something to
 * triage without a live production error source. Disabled by default so the
 * app's normal behavior is unaffected.
 */
@ConfigurationProperties(prefix = "simulator")
data class SimulatorProperties(
    val enabled: Boolean = false,
    val delayMs: Long = 200,
) {
    init {
        require(delayMs >= 0) { "simulator.delay-ms must not be negative: $delayMs" }
    }
}
