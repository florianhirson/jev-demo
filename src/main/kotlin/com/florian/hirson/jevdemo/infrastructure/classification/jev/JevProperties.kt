package com.florian.hirson.jevdemo.infrastructure.classification.jev

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * Tunables for the jev adapter. Deliberately excludes the API key: it comes
 * from the JEV_API_KEY environment variable directly into the RestClient
 * bean, never through a data class whose auto-generated toString() could
 * leak it into a log.
 */
@ConfigurationProperties(prefix = "jev")
data class JevProperties(
    val baseUrl: String = "https://api.typesafe.ai",
    val model: String = "jev-latest",
    val connectTimeout: Duration = Duration.ofSeconds(2),
    val readTimeout: Duration = Duration.ofSeconds(3),
    val maxAttempts: Int = 3,
    val circuitBreakerFailureRateThreshold: Float = 50f,
    val circuitBreakerWaitDurationInOpenState: Duration = Duration.ofSeconds(30),
) {
    init {
        require(maxAttempts > 0) { "jev.max-attempts must be positive: $maxAttempts" }
        require(circuitBreakerFailureRateThreshold > 0f) {
            "jev.circuit-breaker-failure-rate-threshold must be positive: $circuitBreakerFailureRateThreshold"
        }
    }
}
