package com.florian.hirson.jevdemo.domain.triage

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertFailsWith

class LogEventTest {

    @Test
    fun `un message vide est rejete comme BlankLogMessage`() {
        assertFailsWith<BlankLogMessage> {
            LogEvent(
                message = "   ",
                stackTrace = null,
                occurredAt = Instant.parse("2026-09-19T10:15:30Z"),
            )
        }
    }
}
