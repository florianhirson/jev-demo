package com.florian.hirson.jevdemo.domain.triage

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class LogEventRedactionTest {

    private fun logEvent(message: String, stackTrace: String? = null) =
        LogEvent(message = message, stackTrace = stackTrace, occurredAt = Instant.parse("2026-09-19T10:15:30Z"))

    @Test
    fun `une adresse email est remplacee par EMAIL`() {
        val redacted = logEvent("Failed to notify user jane.doe+ops@acme.example about the outage").redactedMessage

        assertEquals("Failed to notify user [EMAIL] about the outage", redacted)
    }

    @Test
    fun `un bearer token est remplace`() {
        val redacted = logEvent("Auth header: Bearer abcDEF123.xyz-token_456").redactedMessage

        assertEquals("Auth header: Bearer [TOKEN]", redacted)
    }

    @Test
    fun `un JWT est remplace`() {
        val jwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3jVmNHl0w5N_XgL0n3I9PYE"
        val redacted = logEvent("token=$jwt").redactedMessage

        assertEquals("token=[TOKEN]", redacted)
    }

    @Test
    fun `une cle d-api en clair est remplacee sans prefixe Bearer`() {
        val redacted = logEvent("api_key=sk_live_aBc123XyZ789LmNoPqRs456 rejected").redactedMessage

        assertEquals("api_key=[TOKEN] rejected", redacted)
    }

    @Test
    fun `une adresse IPv4 est remplacee par IP`() {
        val redacted = logEvent("Connection from 192.168.1.42 refused").redactedMessage

        assertEquals("Connection from [IP] refused", redacted)
    }

    @Test
    fun `un message sans donnee sensible n-est pas modifie`() {
        val redacted = logEvent("NullPointerException at PaymentService.charge").redactedMessage

        assertEquals("NullPointerException at PaymentService.charge", redacted)
    }

    @Test
    fun `la stack trace est redigee comme le message`() {
        val redacted = logEvent("boom", stackTrace = "called from 10.0.0.1 by alice@acme.example").redactedStackTrace

        assertEquals("called from [IP] by [EMAIL]", redacted)
    }

    @Test
    fun `une stack trace absente reste absente apres redaction`() {
        val redacted = logEvent("boom").redactedStackTrace

        assertEquals(null, redacted)
    }
}
