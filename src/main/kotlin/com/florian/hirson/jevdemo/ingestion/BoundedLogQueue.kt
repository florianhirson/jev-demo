package com.florian.hirson.jevdemo.ingestion

import com.florian.hirson.jevdemo.domain.triage.LogEvent
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicLong

/**
 * In-memory backpressure boundary between log ingestion and triage: bounded
 * so a burst of errors can never grow memory without limit. A full queue
 * drops the incoming event rather than blocking the caller (the appender
 * runs on the logging thread of whatever emitted the error) and counts the
 * drop so it can be reported as a metric later (increment 5).
 */
class BoundedLogQueue(capacity: Int) {

    private val queue = ArrayBlockingQueue<LogEvent>(capacity)
    private val droppedCount = AtomicLong(0)

    val dropped: Long get() = droppedCount.get()

    /** Non-blocking: returns whether [logEvent] was accepted. */
    fun offer(logEvent: LogEvent): Boolean {
        val accepted = queue.offer(logEvent)
        if (!accepted) droppedCount.incrementAndGet()
        return accepted
    }

    /** Blocks until an event is available; propagates interruption to the caller. */
    fun take(): LogEvent = queue.take()

    /** Non-blocking: returns the next event, or `null` if none is available right now. */
    fun poll(): LogEvent? = queue.poll()
}
