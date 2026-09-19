package com.florian.hirson.jevdemo.ingestion

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.LoggingEvent
import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TriageLogAppenderTest {

    private fun loggingEvent(
        loggerName: String,
        level: Level,
        message: String,
        throwable: Throwable? = null,
    ): LoggingEvent {
        val logger = LoggerFactory.getLogger(loggerName) as Logger
        return LoggingEvent(
            TriageLogAppenderTest::class.java.name,
            logger,
            level,
            message,
            throwable,
            null,
        )
    }

    @Test
    fun `un log ERROR hors du package de triage est mis en file`() {
        val queue = BoundedLogQueue(capacity = 4)
        val appender = TriageLogAppender(queue)

        appender.append(
            loggingEvent(
                loggerName = "com.acme.payments.PaymentService",
                level = Level.ERROR,
                message = "Connection refused calling payment-service",
                throwable = RuntimeException("boom"),
            ),
        )

        val logEvent = queue.poll()
        assertEquals("Connection refused calling payment-service", logEvent?.message)
        assertTrue(logEvent?.stackTrace?.contains("RuntimeException") == true)
    }

    @Test
    fun `un log en dessous du niveau ERROR est ignore`() {
        val queue = BoundedLogQueue(capacity = 4)
        val appender = TriageLogAppender(queue)

        appender.append(loggingEvent("com.acme.payments.PaymentService", Level.WARN, "just a warning"))

        assertEquals(0, queue.dropped)
        assertNull(queue.poll())
    }

    @Test
    fun `un log ERROR emis par le package de triage lui-meme est ignore`() {
        val queue = BoundedLogQueue(capacity = 4)
        val appender = TriageLogAppender(queue)

        appender.append(
            loggingEvent(
                loggerName = "com.florian.hirson.jevdemo.ingestion.TriageLogConsumer",
                level = Level.ERROR,
                message = "classification failed",
            ),
        )

        assertNull(queue.poll())
    }

    @Test
    fun `un log ERROR sans message ne fait pas echouer l-appender`() {
        val queue = BoundedLogQueue(capacity = 4)
        val appender = TriageLogAppender(queue)

        appender.append(loggingEvent("com.acme.payments.PaymentService", Level.ERROR, "   "))

        assertNull(queue.poll())
    }
}
