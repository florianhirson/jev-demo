package com.florian.hirson.jevdemo.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Tunables for the ingestion pipeline. Defaults are a starting guess for a
 * demo, not a calibrated capacity plan — see increment 6 for calibration
 * against the test dataset.
 */
@ConfigurationProperties(prefix = "triage.ingestion")
data class TriageIngestionProperties(
    val queueCapacity: Int = 1024,
    val consumerCount: Int = 4,
) {
    init {
        require(queueCapacity > 0) { "triage.ingestion.queue-capacity must be positive: $queueCapacity" }
        require(consumerCount > 0) { "triage.ingestion.consumer-count must be positive: $consumerCount" }
    }
}
