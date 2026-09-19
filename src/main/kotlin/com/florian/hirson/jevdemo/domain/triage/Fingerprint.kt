package com.florian.hirson.jevdemo.domain.triage

import java.security.MessageDigest

/**
 * The identity of a "kind of" error, not of one specific occurrence: two
 * [LogEvent]s with the same redacted message share a [Fingerprint], which is
 * what lets a [ClassificationMemory] recognize a recurring error instead of
 * re-classifying it. Wraps a SHA-256 hex digest of [LogEvent.redactedMessage]
 * — the same redacted text jev sees — so two errors differing only by an
 * email, token or IP still fingerprint identically.
 */
data class Fingerprint(val hash: String) {
    init {
        if (!HEX_SHA_256.matches(hash)) throw InvalidFingerprint(hash)
    }

    companion object {
        private val HEX_SHA_256 = Regex("""[0-9a-f]{64}""")

        fun of(logEvent: LogEvent): Fingerprint {
            val digest = MessageDigest.getInstance("SHA-256").digest(logEvent.redactedMessage.toByteArray(Charsets.UTF_8))
            return Fingerprint(digest.joinToString("") { "%02x".format(it) })
        }
    }
}

/** A [Fingerprint] was rejected because it must be a 64-character lowercase hex SHA-256 digest. */
class InvalidFingerprint(hash: String) :
    IllegalArgumentException("Fingerprint must be a 64-character lowercase hex SHA-256 digest: $hash")
