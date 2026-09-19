package com.florian.hirson.jevdemo.domain.triage

import kotlin.test.Test
import kotlin.test.assertEquals

class RedactSensitiveDataTest {

    @Test
    fun `une adresse email est remplacee par EMAIL`() {
        val redacted = RedactSensitiveData.execute("Failed to notify user jane.doe+ops@acme.example about the outage")

        assertEquals("Failed to notify user [EMAIL] about the outage", redacted)
    }

    @Test
    fun `un bearer token est remplace`() {
        val redacted = RedactSensitiveData.execute("Auth header: Bearer abcDEF123.xyz-token_456")

        assertEquals("Auth header: Bearer [TOKEN]", redacted)
    }

    @Test
    fun `un JWT est remplace`() {
        val jwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3jVmNHl0w5N_XgL0n3I9PYE"
        val redacted = RedactSensitiveData.execute("token=$jwt")

        assertEquals("token=[TOKEN]", redacted)
    }

    @Test
    fun `une cle d-api en clair est remplacee sans prefixe Bearer`() {
        val redacted = RedactSensitiveData.execute("api_key=sk_live_aBc123XyZ789LmNoPqRs456 rejected")

        assertEquals("api_key=[TOKEN] rejected", redacted)
    }

    @Test
    fun `une adresse IPv4 est remplacee par IP`() {
        val redacted = RedactSensitiveData.execute("Connection from 192.168.1.42 refused")

        assertEquals("Connection from [IP] refused", redacted)
    }

    @Test
    fun `un message sans donnee sensible n-est pas modifie`() {
        val redacted = RedactSensitiveData.execute("NullPointerException at PaymentService.charge")

        assertEquals("NullPointerException at PaymentService.charge", redacted)
    }
}
