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
        if (message.isBlank()) throw BlankLogMessage
    }
}

/** A [LogEvent] was rejected because its message carries no information to triage on. */
data object BlankLogMessage : IllegalArgumentException("A log event message must not be blank")
