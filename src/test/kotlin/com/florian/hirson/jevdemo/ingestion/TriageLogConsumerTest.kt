package com.florian.hirson.jevdemo.ingestion

import com.florian.hirson.jevdemo.application.triage.usecase.ClassifyLogEventUseCase
import com.florian.hirson.jevdemo.domain.triage.Actionable
import com.florian.hirson.jevdemo.domain.triage.Category
import com.florian.hirson.jevdemo.domain.triage.Classification
import com.florian.hirson.jevdemo.domain.triage.LogClassifier
import com.florian.hirson.jevdemo.domain.triage.LogEvent
import com.florian.hirson.jevdemo.domain.triage.Severity
import java.time.Instant
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TriageLogConsumerTest {

    private class RecordingLogClassifier(private val latch: CountDownLatch) : LogClassifier {
        val received = CopyOnWriteArrayList<LogEvent>()

        override fun classify(logEvent: LogEvent): Classification {
            received.add(logEvent)
            latch.countDown()
            return Classification(Category.NOISE, Severity(0.0), Actionable(0.0))
        }
    }

    private fun logEvent(message: String) =
        LogEvent(message = message, occurredAt = Instant.parse("2026-09-19T10:15:30Z"))

    @Test
    fun `un evenement mis en file est classifie une fois le consommateur demarre`() {
        val queue = BoundedLogQueue(capacity = 4)
        val latch = CountDownLatch(1)
        val classifier = RecordingLogClassifier(latch)
        val consumer = TriageLogConsumer(queue, ClassifyLogEventUseCase(classifier), consumerCount = 1)

        queue.offer(logEvent("connection refused"))
        consumer.start()
        try {
            assertTrue(latch.await(2, TimeUnit.SECONDS), "le classifieur n'a pas été appelé à temps")
            assertEquals(listOf("connection refused"), classifier.received.map { it.message })
        } finally {
            consumer.stop()
        }
    }

    @Test
    fun `le consommateur survit a une classification en echec et continue de traiter la file`() {
        val queue = BoundedLogQueue(capacity = 4)
        val latch = CountDownLatch(1)
        val received = CopyOnWriteArrayList<LogEvent>()
        val classifier = object : LogClassifier {
            override fun classify(logEvent: LogEvent): Classification {
                if (logEvent.message == "boom") throw RuntimeException("jev is down")
                received.add(logEvent)
                latch.countDown()
                return Classification(Category.NOISE, Severity(0.0), Actionable(0.0))
            }
        }
        val consumer = TriageLogConsumer(queue, ClassifyLogEventUseCase(classifier), consumerCount = 1)

        consumer.start()
        try {
            queue.offer(logEvent("boom"))
            queue.offer(logEvent("recovers after the failure"))

            assertTrue(latch.await(2, TimeUnit.SECONDS), "le consommateur s'est arrêté après l'échec au lieu de continuer")
            assertEquals(listOf("recovers after the failure"), received.map { it.message })
        } finally {
            consumer.stop()
        }
    }

    @Test
    fun `plusieurs evenements sont tous classifies`() {
        val queue = BoundedLogQueue(capacity = 4)
        val latch = CountDownLatch(3)
        val classifier = RecordingLogClassifier(latch)
        val consumer = TriageLogConsumer(queue, ClassifyLogEventUseCase(classifier), consumerCount = 2)

        consumer.start()
        try {
            queue.offer(logEvent("first"))
            queue.offer(logEvent("second"))
            queue.offer(logEvent("third"))

            assertTrue(latch.await(2, TimeUnit.SECONDS), "les 3 événements n'ont pas tous été classifiés à temps")
            assertEquals(setOf("first", "second", "third"), classifier.received.map { it.message }.toSet())
        } finally {
            consumer.stop()
        }
    }
}
