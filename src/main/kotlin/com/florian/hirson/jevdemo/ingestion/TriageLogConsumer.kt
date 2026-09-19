package com.florian.hirson.jevdemo.ingestion

import com.florian.hirson.jevdemo.application.triage.usecase.ClassifyLogEventUseCase
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Drains [queue] on a fixed pool of virtual threads, driving
 * [classifyLogEvent] for each [com.florian.hirson.jevdemo.domain.triage.LogEvent].
 * Concurrency is bounded by [consumerCount] rather than one thread per event:
 * virtual threads are cheap, but the point of the queue is to bound how much
 * triage work runs at once, not to remove that bound.
 */
class TriageLogConsumer(
    private val queue: BoundedLogQueue,
    private val classifyLogEvent: ClassifyLogEventUseCase,
    private val consumerCount: Int,
) {
    private val running = AtomicBoolean(false)
    private var executor: ExecutorService? = null

    fun start() {
        running.set(true)
        val pool = Executors.newFixedThreadPool(consumerCount, Thread.ofVirtual().factory())
        executor = pool
        repeat(consumerCount) { pool.execute(::consumeUntilStopped) }
    }

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
            classifyLogEvent.execute(logEvent)
        }
    }
}
