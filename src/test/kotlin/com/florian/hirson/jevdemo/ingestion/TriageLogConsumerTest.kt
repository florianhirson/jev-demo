package com.florian.hirson.jevdemo.ingestion

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.florian.hirson.jevdemo.application.triage.usecase.TriageLogEventUseCase
import com.florian.hirson.jevdemo.application.triage.usecase.TriageOutcome
import com.florian.hirson.jevdemo.application.triage.usecase.TriageOutcomePresenter
import com.florian.hirson.jevdemo.domain.triage.Actionable
import com.florian.hirson.jevdemo.domain.triage.Category
import com.florian.hirson.jevdemo.domain.triage.Classification
import com.florian.hirson.jevdemo.domain.triage.Confidence
import com.florian.hirson.jevdemo.domain.triage.LogClassifier
import com.florian.hirson.jevdemo.domain.triage.LogEvent
import com.florian.hirson.jevdemo.domain.triage.ReviewQueue
import com.florian.hirson.jevdemo.domain.triage.RoutingDecision
import com.florian.hirson.jevdemo.domain.triage.RoutingThresholds
import com.florian.hirson.jevdemo.domain.triage.Severity
import com.florian.hirson.jevdemo.domain.triage.TriageMetrics
import com.florian.hirson.jevdemo.infrastructure.presentation.SilentTriageOutcomePresenter
import com.florian.hirson.jevdemo.infrastructure.review.InMemoryReviewQueue
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TriageLogConsumerTest {

    private class RecordingLogClassifier(private val latch: CountDownLatch) : LogClassifier {
        val received = CopyOnWriteArrayList<LogEvent>()

        override fun classify(logEvent: LogEvent): Classification {
            received.add(logEvent)
            latch.countDown()
            return Classification(
                category = Category.NOISE,
                categoryConfidence = Confidence(1.0),
                severity = Severity(0.0),
                severityConfidence = Confidence(1.0),
                actionable = Actionable(0.0),
            )
        }
    }

    private class RecordingTriageMetrics : TriageMetrics {
        var triageFailures = 0
            private set

        override fun recordRouted(classification: Classification, decision: RoutingDecision) {
            // not exercised by this test suite
        }

        override fun recordTriageFailed() {
            triageFailures++
        }
    }

    private fun logEvent(message: String) =
        LogEvent(message = message, occurredAt = Instant.parse("2026-09-19T10:15:30Z"))

    private fun triageLogEventUseCase(
        classifier: LogClassifier,
        reviewQueue: ReviewQueue = InMemoryReviewQueue(),
        metrics: TriageMetrics = RecordingTriageMetrics(),
    ) = TriageLogEventUseCase(
        classifier,
        reviewQueue,
        RoutingThresholds(categoryConfidence = Confidence(0.7), severityConfidence = Confidence(0.7), actionableConfidence = Confidence(0.7)),
        metrics,
    )

    @Test
    fun `un evenement mis en file est classifie une fois le consommateur demarre`() {
        val queue = BoundedLogQueue(capacity = 4)
        val latch = CountDownLatch(1)
        val classifier = RecordingLogClassifier(latch)
        val consumer = TriageLogConsumer(queue, triageLogEventUseCase(classifier), RecordingTriageMetrics(), consumerCount = 1, presenter = SilentTriageOutcomePresenter)

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
                return Classification(
                    category = Category.NOISE,
                    categoryConfidence = Confidence(1.0),
                    severity = Severity(0.0),
                    severityConfidence = Confidence(1.0),
                    actionable = Actionable(0.0),
                )
            }
        }
        val metrics = RecordingTriageMetrics()
        val consumer = TriageLogConsumer(queue, triageLogEventUseCase(classifier), metrics, consumerCount = 1, presenter = SilentTriageOutcomePresenter)

        consumer.start()
        try {
            queue.offer(logEvent("boom"))
            queue.offer(logEvent("recovers after the failure"))

            assertTrue(latch.await(2, TimeUnit.SECONDS), "le consommateur s'est arrêté après l'échec au lieu de continuer")
            assertEquals(listOf("recovers after the failure"), received.map { it.message })
            assertEquals(1, metrics.triageFailures)
        } finally {
            consumer.stop()
        }
    }

    @Test
    fun `l-echec d-une classification est journalise sans le message brut`() {
        // Régression : ce log ne doit jamais répéter en clair ce que la
        // rédaction (LogEvent.redactedMessage) existe pour cacher à jev.
        val appender = ListAppender<ILoggingEvent>()
        appender.start()
        val logger = LoggerFactory.getLogger(TriageLogConsumer::class.java) as Logger
        logger.addAppender(appender)

        val queue = BoundedLogQueue(capacity = 4)
        val classifier = object : LogClassifier {
            override fun classify(logEvent: LogEvent): Classification = throw RuntimeException("jev is down")
        }
        val consumer = TriageLogConsumer(queue, triageLogEventUseCase(classifier), RecordingTriageMetrics(), consumerCount = 1, presenter = SilentTriageOutcomePresenter)

        consumer.start()
        try {
            queue.offer(logEvent("Timeout calling 10.0.0.1 for user alice@acme.example"))

            val loggedMessage = awaitFirstLogMessage(appender)
            assertTrue(loggedMessage.contains("[IP]"))
            assertTrue(loggedMessage.contains("[EMAIL]"))
            assertFalse(loggedMessage.contains("10.0.0.1"))
            assertFalse(loggedMessage.contains("alice@acme.example"))
        } finally {
            consumer.stop()
            logger.detachAppender(appender)
        }
    }

    private fun awaitFirstLogMessage(appender: ListAppender<ILoggingEvent>): String {
        val deadline = System.currentTimeMillis() + 2000
        while (appender.list.isEmpty() && System.currentTimeMillis() < deadline) {
            Thread.sleep(10)
        }
        return appender.list.first().formattedMessage
    }

    @Test
    fun `plusieurs evenements sont tous classifies`() {
        val queue = BoundedLogQueue(capacity = 4)
        val latch = CountDownLatch(3)
        val classifier = RecordingLogClassifier(latch)
        val consumer = TriageLogConsumer(queue, triageLogEventUseCase(classifier), RecordingTriageMetrics(), consumerCount = 2, presenter = SilentTriageOutcomePresenter)

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

    private class RecordingPresenter : TriageOutcomePresenter {
        val presented = CopyOnWriteArrayList<Pair<LogEvent, TriageOutcome>>()
        val failures = CopyOnWriteArrayList<LogEvent>()

        override fun present(logEvent: LogEvent, outcome: TriageOutcome) {
            presented.add(logEvent to outcome)
        }

        override fun presentTriageFailure(logEvent: LogEvent) {
            failures.add(logEvent)
        }
    }

    @Test
    fun `l-issue du triage et son evenement sont presentes, et un echec l-est aussi`() {
        val queue = BoundedLogQueue(capacity = 4)
        val classifier = object : LogClassifier {
            override fun classify(logEvent: LogEvent): Classification {
                if (logEvent.message == "boom") throw RuntimeException("jev is down")
                return Classification(
                    category = Category.NOISE,
                    categoryConfidence = Confidence(1.0),
                    severity = Severity(0.0),
                    severityConfidence = Confidence(1.0),
                    actionable = Actionable(0.0),
                )
            }
        }
        val presenter = RecordingPresenter()
        val consumer = TriageLogConsumer(
            queue, triageLogEventUseCase(classifier), RecordingTriageMetrics(), consumerCount = 1, presenter = presenter,
        )

        consumer.start()
        try {
            queue.offer(logEvent("fine"))
            queue.offer(logEvent("boom"))

            val deadline = System.currentTimeMillis() + 2000
            while ((presenter.presented.isEmpty() || presenter.failures.isEmpty()) && System.currentTimeMillis() < deadline) {
                Thread.sleep(10)
            }
            assertEquals(listOf("fine"), presenter.presented.map { it.first.message })
            assertEquals(RoutingDecision.AUTOMATIC, presenter.presented.single().second.decision)
            assertEquals(listOf("boom"), presenter.failures.map { it.message })
        } finally {
            consumer.stop()
        }
    }

    @Test
    fun `une erreur d-affichage n-est pas un echec de triage et ne tue pas le consommateur`() {
        val queue = BoundedLogQueue(capacity = 4)
        val latch = CountDownLatch(2)
        val classifier = RecordingLogClassifier(latch)
        val metrics = RecordingTriageMetrics()
        val brokenPresenter = object : TriageOutcomePresenter {
            override fun present(logEvent: LogEvent, outcome: TriageOutcome) = throw IllegalStateException("terminal closed")
            override fun presentTriageFailure(logEvent: LogEvent) = Unit
        }
        val consumer = TriageLogConsumer(queue, triageLogEventUseCase(classifier), metrics, consumerCount = 1, presenter = brokenPresenter)

        consumer.start()
        try {
            queue.offer(logEvent("first"))
            queue.offer(logEvent("second"))

            assertTrue(latch.await(2, TimeUnit.SECONDS), "le consommateur s'est arrêté après l'erreur d'affichage")
            assertEquals(0, metrics.triageFailures)
        } finally {
            consumer.stop()
        }
    }

    @Test
    fun `une erreur d-affichage d-un echec n-empeche ni le log d-erreur ni la suite du traitement`() {
        val appender = ListAppender<ILoggingEvent>()
        appender.start()
        val logger = LoggerFactory.getLogger(TriageLogConsumer::class.java) as Logger
        logger.addAppender(appender)

        val queue = BoundedLogQueue(capacity = 4)
        val latch = CountDownLatch(1)
        val received = CopyOnWriteArrayList<LogEvent>()
        val classifier = object : LogClassifier {
            override fun classify(logEvent: LogEvent): Classification {
                if (logEvent.message == "boom") throw RuntimeException("jev is down")
                received.add(logEvent)
                latch.countDown()
                return Classification(
                    category = Category.NOISE,
                    categoryConfidence = Confidence(1.0),
                    severity = Severity(0.0),
                    severityConfidence = Confidence(1.0),
                    actionable = Actionable(0.0),
                )
            }
        }
        val brokenPresenter = object : TriageOutcomePresenter {
            override fun present(logEvent: LogEvent, outcome: TriageOutcome) = Unit
            override fun presentTriageFailure(logEvent: LogEvent) = throw IllegalStateException("terminal closed")
        }
        val consumer = TriageLogConsumer(queue, triageLogEventUseCase(classifier), RecordingTriageMetrics(), consumerCount = 1, presenter = brokenPresenter)

        consumer.start()
        try {
            queue.offer(logEvent("boom"))
            queue.offer(logEvent("still processed"))

            assertTrue(latch.await(2, TimeUnit.SECONDS), "le consommateur s'est arrêté après l'erreur d'affichage d'un échec")
            assertTrue(appender.list.any { it.formattedMessage.startsWith("Failed to triage log event") })
        } finally {
            consumer.stop()
            logger.detachAppender(appender)
        }
    }
}
