package com.florian.hirson.jevdemo.domain.triage

/**
 * A [LogEvent] together with the [Classification] jev produced for it, held
 * so a human can look at it — [TriageLogEventUseCase] only ever constructs
 * one when [Classification.route] returns [RoutingDecision.FOR_REVIEW], but
 * this type doesn't enforce that itself, the same way [ClassificationMemory]
 * doesn't police what gets remembered. [logEvent] already carries its own
 * [LogEvent.occurredAt], so no separate timestamp is tracked here.
 */
data class ReviewCase(val logEvent: LogEvent, val classification: Classification)
