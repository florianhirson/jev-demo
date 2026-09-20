package com.florian.hirson.jevdemo.domain.triage

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The contract every [ReviewQueue] adapter must satisfy, run against each
 * adapter in turn so they stay interchangeable behind the port.
 */
abstract class ReviewQueueContract {

    protected abstract fun newQueue(): ReviewQueue

    private fun reviewCase(message: String) = ReviewCase(
        logEvent = LogEvent(message = message, occurredAt = Instant.parse("2026-09-19T10:15:30Z")),
        classification = Classification(
            category = Category.EXTERNAL_DEPENDENCY,
            categoryConfidence = Confidence(0.5),
            severity = Severity(0.8),
            severityConfidence = Confidence(0.5),
            actionable = Actionable(0.5),
        ),
    )

    @Test
    fun `une file neuve n-a aucune revue en attente`() {
        assertEquals(emptyList(), newQueue().pending())
    }

    @Test
    fun `submit rend le cas visible dans pending`() {
        val queue = newQueue()
        val reviewCase = reviewCase("Connection refused calling payment-service")

        queue.submit(reviewCase)

        assertEquals(listOf(reviewCase), queue.pending())
    }

    @Test
    fun `plusieurs submit s-accumulent dans l-ordre`() {
        val queue = newQueue()
        val first = reviewCase("first")
        val second = reviewCase("second")

        queue.submit(first)
        queue.submit(second)

        assertEquals(listOf(first, second), queue.pending())
    }
}
