package com.florian.hirson.jevdemo.tooling

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.florian.hirson.jevdemo.config.SimulatorProperties
import com.florian.hirson.jevdemo.domain.evaluation.ErrorsDataset
import com.florian.hirson.jevdemo.domain.evaluation.LabeledLogEvent
import com.florian.hirson.jevdemo.domain.triage.Category
import com.florian.hirson.jevdemo.domain.triage.LogEvent
import org.slf4j.LoggerFactory
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class LogSimulatorRunnerTest {

    private class FixedErrorsDataset(private val entries: List<LabeledLogEvent>) : ErrorsDataset {
        override fun load(): List<LabeledLogEvent> = entries
    }

    private fun logEvent(message: String, stackTrace: String? = null) =
        LogEvent(message = message, stackTrace = stackTrace, occurredAt = Instant.parse("2026-09-20T10:00:00Z"))

    @Test
    fun `chaque evenement du jeu de donnees est rejoue comme un log ERROR`() {
        val dataset = FixedErrorsDataset(
            listOf(
                LabeledLogEvent(logEvent("Connection refused calling payment-service"), Category.EXTERNAL_DEPENDENCY),
                LabeledLogEvent(
                    logEvent("NullPointerException while resolving customer", stackTrace = "java.lang.NullPointerException"),
                    Category.APPLICATION_BUG,
                ),
            ),
        )
        val logbackLogger = LoggerFactory.getLogger(LogSimulatorRunner::class.java) as Logger
        val appender = ListAppender<ILoggingEvent>()
        appender.start()
        logbackLogger.addAppender(appender)

        try {
            LogSimulatorRunner(dataset, SimulatorProperties(enabled = true, delayMs = 0)).run()
        } finally {
            logbackLogger.detachAppender(appender)
        }

        assertEquals(2, appender.list.size)
        assertEquals("Connection refused calling payment-service", appender.list[0].message)
        assertNull(appender.list[0].throwableProxy)
        assertEquals("NullPointerException while resolving customer", appender.list[1].message)
        assertNotNull(appender.list[1].throwableProxy)
    }
}
