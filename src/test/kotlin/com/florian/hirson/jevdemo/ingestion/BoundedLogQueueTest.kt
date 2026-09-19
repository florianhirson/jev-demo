package com.florian.hirson.jevdemo.ingestion

import com.florian.hirson.jevdemo.domain.triage.LogEvent
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BoundedLogQueueTest {

    private fun logEvent(message: String) = LogEvent(
        message = message,
        occurredAt = Instant.parse("2026-09-19T10:15:30Z"),
    )

    @Test
    fun `un evenement est accepte tant que la capacite n-est pas atteinte`() {
        val queue = BoundedLogQueue(capacity = 2)

        assertTrue(queue.offer(logEvent("first")))
        assertTrue(queue.offer(logEvent("second")))
        assertEquals(0, queue.dropped)
    }

    @Test
    fun `un evenement est abandonne et compte quand la file est pleine`() {
        val queue = BoundedLogQueue(capacity = 1)
        queue.offer(logEvent("first"))

        val accepted = queue.offer(logEvent("second"))

        assertFalse(accepted)
        assertEquals(1, queue.dropped)
    }

    @Test
    fun `les evenements sont consommes dans l-ordre d-arrivee`() {
        val queue = BoundedLogQueue(capacity = 2)
        queue.offer(logEvent("first"))
        queue.offer(logEvent("second"))

        assertEquals("first", queue.take().message)
        assertEquals("second", queue.take().message)
    }
}
