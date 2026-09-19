package com.florian.hirson.jevdemo.domain.triage

import net.jqwik.api.Arbitraries
import net.jqwik.api.Arbitrary
import net.jqwik.api.Combinators
import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.Provide
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Property-based coverage for [LogEvent.redactedMessage], per the project's
 * convention of testing fingerprint normalization on generated inputs rather
 * than a handful of hard-coded examples.
 */
class LogEventRedactionPropertyTest {

    private fun logEvent(message: String) =
        LogEvent(message = message, occurredAt = Instant.parse("2026-09-19T10:15:30Z"))

    @Property
    fun `la redaction est idempotente`(@ForAll("noisyText") text: String) {
        val once = logEvent(text).redactedMessage
        val twice = logEvent(once).redactedMessage

        assertEquals(once, twice)
    }

    @Property
    fun `une adresse IPv4 generee n-apparait plus apres redaction`(
        @ForAll("word") before: String,
        @ForAll("ipv4") ip: String,
        @ForAll("word") after: String,
    ) {
        val redacted = logEvent("$before $ip $after").redactedMessage

        assertTrue(redacted.contains("[IP]"))
        assertFalse(redacted.contains(ip))
    }

    @Property
    fun `un email genere n-apparait plus apres redaction`(
        @ForAll("word") before: String,
        @ForAll("email") email: String,
        @ForAll("word") after: String,
    ) {
        val redacted = logEvent("$before $email $after").redactedMessage

        assertTrue(redacted.contains("[EMAIL]"))
        assertFalse(redacted.contains("@"))
    }

    @Provide
    fun noisyText(): Arbitrary<String> = Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(40)

    @Provide
    fun word(): Arbitrary<String> = Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(10)

    @Provide
    fun ipv4(): Arbitrary<String> {
        val octet = Arbitraries.integers().between(0, 255)
        return Combinators.combine(octet, octet, octet, octet).`as` { a, b, c, d -> "$a.$b.$c.$d" }
    }

    @Provide
    fun email(): Arbitrary<String> {
        val local = Arbitraries.strings().withCharRange('a', 'z').ofMinLength(3).ofMaxLength(10)
        val domain = Arbitraries.strings().withCharRange('a', 'z').ofMinLength(3).ofMaxLength(10)
        val tld = Arbitraries.strings().withCharRange('a', 'z').ofMinLength(2).ofMaxLength(3)
        return Combinators.combine(local, domain, tld).`as` { l, d, t -> "$l@$d.$t" }
    }
}
