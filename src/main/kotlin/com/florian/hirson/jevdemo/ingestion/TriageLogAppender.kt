package com.florian.hirson.jevdemo.ingestion

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.ThrowableProxyUtil
import ch.qos.logback.core.AppenderBase
import com.florian.hirson.jevdemo.domain.triage.BlankLogMessage
import com.florian.hirson.jevdemo.domain.triage.LogEvent
import java.time.Instant

/**
 * Driving adapter: turns Logback ERROR events into [LogEvent]s and hands
 * them to the [queue] for triage. [excludedLoggerPrefixes] keeps the
 * ingestion pipeline's own logs out of triage — without it, a log emitted
 * while triaging a log would triage itself, forever.
 */
class TriageLogAppender(
    private val queue: BoundedLogQueue,
    private val excludedLoggerPrefixes: List<String> = listOf("com.florian.hirson.jevdemo"),
) : AppenderBase<ILoggingEvent>() {

    public override fun append(event: ILoggingEvent) {
        if (event.level != Level.ERROR) return
        if (excludedLoggerPrefixes.any { event.loggerName.startsWith(it) }) return

        val logEvent = toLogEventOrNull(event) ?: return
        queue.offer(logEvent)
    }

    private fun toLogEventOrNull(event: ILoggingEvent): LogEvent? = try {
        LogEvent(
            message = event.formattedMessage,
            stackTrace = event.throwableProxy?.let { ThrowableProxyUtil.asString(it) },
            occurredAt = Instant.ofEpochMilli(event.timeStamp),
        )
    } catch (_: BlankLogMessage) {
        null
    }
}
