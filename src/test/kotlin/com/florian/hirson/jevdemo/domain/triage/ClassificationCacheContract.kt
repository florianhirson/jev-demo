package com.florian.hirson.jevdemo.domain.triage

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * The contract every [ClassificationCache] adapter must satisfy, run against
 * each adapter in turn so they stay interchangeable behind the port.
 */
abstract class ClassificationCacheContract {

    protected abstract fun newCache(): ClassificationCache

    private val fingerprint = Fingerprint.of(
        LogEvent(message = "Connection refused calling payment-service", occurredAt = java.time.Instant.parse("2026-09-19T10:15:30Z")),
    )
    private val classification = Classification(Category.EXTERNAL_DEPENDENCY, Severity(0.8), Actionable(0.9))

    @Test
    fun `get sur une empreinte inconnue renvoie null`() {
        val cache = newCache()

        assertNull(cache.get(fingerprint))
    }

    @Test
    fun `put puis get renvoie la meme classification`() {
        val cache = newCache()

        cache.put(fingerprint, classification)

        assertEquals(classification, cache.get(fingerprint))
    }

    @Test
    fun `put ecrase la valeur precedente pour la meme empreinte`() {
        val cache = newCache()
        val updated = Classification(Category.NOISE, Severity(0.1), Actionable(0.1))

        cache.put(fingerprint, classification)
        cache.put(fingerprint, updated)

        assertEquals(updated, cache.get(fingerprint))
    }
}
