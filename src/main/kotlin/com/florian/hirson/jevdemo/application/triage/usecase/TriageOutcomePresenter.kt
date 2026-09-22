package com.florian.hirson.jevdemo.application.triage.usecase

import com.florian.hirson.jevdemo.domain.triage.LogEvent

/**
 * Port: showing a human what triage decided for a [LogEvent], as it happens.
 * Separate from `TriageMetrics` (aggregates for a scraper) because a person
 * watching needs the event itself next to its [TriageOutcome].
 */
interface TriageOutcomePresenter {
    fun present(logEvent: LogEvent, outcome: TriageOutcome)
    fun presentTriageFailure(logEvent: LogEvent)
}
