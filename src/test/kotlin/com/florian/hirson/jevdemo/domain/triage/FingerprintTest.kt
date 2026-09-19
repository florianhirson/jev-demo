package com.florian.hirson.jevdemo.domain.triage

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class FingerprintTest {

    private fun logEvent(message: String) =
        LogEvent(message = message, occurredAt = Instant.parse("2026-09-19T10:15:30Z"))

    @Test
    fun `un hash invalide est rejete`() {
        assertFailsWith<InvalidFingerprint> { Fingerprint("not-a-hash") }
    }

    @Test
    fun `deux log events au message identique ont la meme empreinte`() {
        val a = Fingerprint.of(logEvent("Connection refused calling payment-service"))
        val b = Fingerprint.of(logEvent("Connection refused calling payment-service"))

        assertEquals(a, b)
    }

    @Test
    fun `deux log events au message different ont une empreinte differente`() {
        val a = Fingerprint.of(logEvent("Connection refused calling payment-service"))
        val b = Fingerprint.of(logEvent("Connection refused calling shipping-service"))

        assertNotEquals(a, b)
    }

    @Test
    fun `deux log events differant seulement par une donnee sensible ont la meme empreinte`() {
        val a = Fingerprint.of(logEvent("Timeout calling 10.0.0.1 for user alice@acme.example"))
        val b = Fingerprint.of(logEvent("Timeout calling 10.0.0.2 for user bob@acme.example"))

        assertEquals(a, b)
    }
}
