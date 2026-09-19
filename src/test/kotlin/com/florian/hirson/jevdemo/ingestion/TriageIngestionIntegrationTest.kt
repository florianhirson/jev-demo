package com.florian.hirson.jevdemo.ingestion

import com.florian.hirson.jevdemo.domain.triage.Actionable
import com.florian.hirson.jevdemo.domain.triage.Category
import com.florian.hirson.jevdemo.domain.triage.Classification
import com.florian.hirson.jevdemo.domain.triage.LogClassifier
import com.florian.hirson.jevdemo.domain.triage.LogEvent
import com.florian.hirson.jevdemo.domain.triage.Severity
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Wires the real Spring context (appender installed on the root logger,
 * queue, virtual-thread consumer, use case) and swaps only the classifier
 * for a recording double — proving the pipeline built in this increment
 * actually runs when the application starts, not just in isolated unit
 * tests.
 */
@SpringBootTest
class TriageIngestionIntegrationTest {

    class RecordingLogClassifier : LogClassifier {
        val latch = CountDownLatch(1)
        val received = CopyOnWriteArrayList<LogEvent>()

        override fun classify(logEvent: LogEvent): Classification {
            received.add(logEvent)
            latch.countDown()
            return Classification(Category.NOISE, Severity(0.0), Actionable(0.0))
        }
    }

    @TestConfiguration
    class RecordingClassifierConfiguration {
        @Bean
        @Primary
        fun recordingLogClassifier(): RecordingLogClassifier = RecordingLogClassifier()
    }

    @Autowired
    lateinit var recordingLogClassifier: RecordingLogClassifier

    @Test
    fun `un log ERROR emis via SLF4J est classifie de bout en bout`() {
        val logger = LoggerFactory.getLogger("com.acme.payments.PaymentService")

        logger.error("Connection refused calling payment-service")

        assertTrue(recordingLogClassifier.latch.await(2, TimeUnit.SECONDS), "le pipeline n'a pas classifié le log à temps")
        assertEquals(listOf("Connection refused calling payment-service"), recordingLogClassifier.received.map { it.message })
    }
}
