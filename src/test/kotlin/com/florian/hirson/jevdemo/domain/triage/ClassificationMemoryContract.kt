package com.florian.hirson.jevdemo.domain.triage

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * The contract every [ClassificationMemory] adapter must satisfy, run against
 * each adapter in turn so they stay interchangeable behind the port.
 */
abstract class ClassificationMemoryContract {

    protected abstract fun newMemory(): ClassificationMemory

    private val fingerprint = Fingerprint.of(
        LogEvent(message = "Connection refused calling payment-service", occurredAt = java.time.Instant.parse("2026-09-19T10:15:30Z")),
    )
    private val classification = Classification(Category.EXTERNAL_DEPENDENCY, Confidence(0.95), Severity(0.8), Confidence(0.9), Actionable(0.9))

    @Test
    fun `recall sur une empreinte inconnue renvoie null`() {
        val memory = newMemory()

        assertNull(memory.recall(fingerprint))
    }

    @Test
    fun `remember puis recall renvoie la meme classification`() {
        val memory = newMemory()

        memory.remember(fingerprint, classification)

        assertEquals(classification, memory.recall(fingerprint))
    }

    @Test
    fun `remember ecrase la valeur precedente pour la meme empreinte`() {
        val memory = newMemory()
        val updated = Classification(Category.NOISE, Confidence(0.5), Severity(0.1), Confidence(0.5), Actionable(0.1))

        memory.remember(fingerprint, classification)
        memory.remember(fingerprint, updated)

        assertEquals(updated, memory.recall(fingerprint))
    }
}
