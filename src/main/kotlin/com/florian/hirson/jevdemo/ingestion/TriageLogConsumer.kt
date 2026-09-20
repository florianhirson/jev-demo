package com.florian.hirson.jevdemo.ingestion

import com.florian.hirson.jevdemo.application.triage.usecase.TriageLogEventUseCase
import com.florian.hirson.jevdemo.domain.triage.TriageMetrics
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Drains [queue] on a fixed pool of virtual threads, driving
 * [triageLogEvent] for each [com.florian.hirson.jevdemo.domain.triage.LogEvent].
 * Concurrency is bounded by [consumerCount] rather than one thread per event:
 * virtual threads are cheap, but the point of the queue is to bound how much
 * triage work runs at once, not to remove that bound.
 *
 * A failed classification (from increment 3 onward: a real network call to
 * jev, which can time out or error) is logged and the loop continues rather
 * than propagating — each consumer is started exactly once, so an
 * unhandled exception here would silently and permanently shrink the pool
 * until nothing drains the queue at all.
 */
class TriageLogConsumer(
    private val queue: BoundedLogQueue,
    private val triageLogEvent: TriageLogEventUseCase,
    private val metrics: TriageMetrics,
    private val consumerCount: Int,
) {
    private val logger = LoggerFactory.getLogger(TriageLogConsumer::class.java)
    private val running = AtomicBoolean(false)
    private var executor: ExecutorService? = null

    @PostConstruct
    fun start() {
        running.set(true)
        val pool = Executors.newFixedThreadPool(consumerCount, Thread.ofVirtual().factory())
        executor = pool
        repeat(consumerCount) { pool.execute(::consumeUntilStopped) }
    }

    @PreDestroy
    fun stop() {
        running.set(false)
        executor?.shutdownNow()
        executor = null
    }

    private fun consumeUntilStopped() {
        while (running.get()) {
            val logEvent = try {
                queue.take()
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                return
            }
            try {
                triageLogEvent.execute(logEvent)
            } catch (exception: Exception) {
                metrics.recordClassificationFailed()
                // redactedMessage, not message: this log line must not repeat whatever
                // sensitive data classification was already trying to keep out of jev.
                logger.error("Failed to triage log event: {}", logEvent.redactedMessage, exception)
            }
        }
    }
}
