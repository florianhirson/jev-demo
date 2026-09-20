package com.florian.hirson.jevdemo.ingestion

import com.florian.hirson.jevdemo.domain.triage.Actionable
import com.florian.hirson.jevdemo.domain.triage.Category
import com.florian.hirson.jevdemo.domain.triage.Classification
import com.florian.hirson.jevdemo.domain.triage.Confidence
import com.florian.hirson.jevdemo.domain.triage.LogClassifier
import com.florian.hirson.jevdemo.domain.triage.LogEvent
import com.florian.hirson.jevdemo.domain.triage.Severity
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Proves the metrics recorded by [com.florian.hirson.jevdemo.infrastructure.observability.MicrometerTriageMetrics]
 * during a real triage actually surface on `/actuator/prometheus` — the
 * "exposition Actuator" half of increment 5, not just the mapping logic
 * covered by MicrometerTriageMetricsTest. A dedicated context (own
 * [TestConfiguration]) so it never shares its classifier singleton with
 * [TriageIngestionIntegrationTest].
 */
@SpringBootTest
@AutoConfigureMockMvc
class TriageObservabilityIntegrationTest {

    class StubLogClassifier : LogClassifier {
        override fun classify(logEvent: LogEvent): Classification {
            return Classification(
                category = Category.EXTERNAL_DEPENDENCY,
                categoryConfidence = Confidence(1.0),
                severity = Severity(0.0),
                severityConfidence = Confidence(1.0),
                actionable = Actionable(0.0),
            )
        }
    }

    @TestConfiguration
    class StubClassifierConfiguration {
        @Bean
        @Primary
        fun stubLogClassifier(): StubLogClassifier = StubLogClassifier()
    }

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `une classification routee est exposee sur -actuator-prometheus`() {
        val logger = LoggerFactory.getLogger("com.acme.orders.OrderService")

        logger.error("Timeout calling shipping-service")

        val prometheusBody = awaitPrometheusBodyContaining("triage_routed_total")
        assertTrue(prometheusBody.contains("category=\"EXTERNAL_DEPENDENCY\""))
    }

    /**
     * Polls instead of asserting once: the consumer processes the log event
     * (classify → route → record metric) on its own virtual thread, so there
     * is no single signal this test can wait on before the metric is
     * guaranteed to have landed in the registry.
     */
    private fun awaitPrometheusBodyContaining(needle: String): String {
        val deadline = System.currentTimeMillis() + 2000
        while (System.currentTimeMillis() < deadline) {
            val body = mockMvc.perform(get("/actuator/prometheus")).andReturn().response.contentAsString
            if (body.contains(needle)) return body
            Thread.sleep(20)
        }
        error("'$needle' never appeared on /actuator/prometheus within the deadline")
    }
}
