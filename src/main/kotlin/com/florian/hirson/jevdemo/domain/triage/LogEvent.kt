package com.florian.hirson.jevdemo.domain.triage

import java.time.Instant

/**
 * An application ERROR log line to triage. [occurredAt] is passed explicitly
 * rather than defaulted to "now" so the domain stays deterministic and
 * testable.
 */
data class LogEvent(
    val message: String,
    val stackTrace: String? = null,
    val occurredAt: Instant,
) {
    init {
        if (message.isBlank()) throw BlankLogMessage(message)
    }

    /**
     * [message] with emails, tokens/API keys and IPv4 addresses replaced by
     * a stable placeholder — safe to send to jev, and the basis of
     * [Fingerprint.of]: two events differing only by one of these values
     * still fingerprint identically.
     */
    val redactedMessage: String get() = redact(message)

    /** [stackTrace] redacted the same way as [redactedMessage], if present. */
    val redactedStackTrace: String? get() = stackTrace?.let(::redact)

    private companion object {
        val EMAIL = Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""")
        val BEARER_TOKEN = Regex("""(?i)\bBearer\s+\S+""")
        val JWT = Regex("""\b[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}\b""")
        val GENERIC_TOKEN = Regex("""\b(?=[A-Za-z0-9_-]*[A-Za-z])(?=[A-Za-z0-9_-]*\d)[A-Za-z0-9_-]{20,}\b""")
        val IPV4 = Regex("""\b(?:(?:25[0-5]|2[0-4]\d|1?\d?\d)\.){3}(?:25[0-5]|2[0-4]\d|1?\d?\d)\b""")

        fun redact(text: String): String {
            var result = text
            result = EMAIL.replace(result, "[EMAIL]")
            result = BEARER_TOKEN.replace(result, "Bearer [TOKEN]")
            result = JWT.replace(result, "[TOKEN]")
            result = GENERIC_TOKEN.replace(result, "[TOKEN]")
            result = IPV4.replace(result, "[IP]")
            return result
        }
    }
}

/** A [LogEvent] was rejected because its message carries no information to triage on. */
class BlankLogMessage(message: String) :
    IllegalArgumentException("A log event message must not be blank: '$message'")
