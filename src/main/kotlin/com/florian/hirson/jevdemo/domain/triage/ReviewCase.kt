package com.florian.hirson.jevdemo.domain.triage

/**
 * A [LogEvent] whose [classification] cleared confidence-based routing into
 * [RoutingDecision.FOR_REVIEW] (see [Classification.route]) — a human, not
 * the pipeline, decides what happens next. [logEvent] already carries its
 * own [LogEvent.occurredAt], so no separate timestamp is tracked here.
 */
data class ReviewCase(val logEvent: LogEvent, val classification: Classification)
