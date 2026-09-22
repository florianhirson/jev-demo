package com.florian.hirson.jevdemo.infrastructure.presentation

import com.florian.hirson.jevdemo.application.triage.usecase.TriageOutcome
import com.florian.hirson.jevdemo.application.triage.usecase.TriageOutcomePresenter
import com.florian.hirson.jevdemo.domain.triage.LogEvent

/** Presents nothing — bound unless a demo turns the console output on, so triage stays quiet by default. */
object SilentTriageOutcomePresenter : TriageOutcomePresenter {
    override fun present(logEvent: LogEvent, outcome: TriageOutcome) = Unit
    override fun presentTriageFailure(logEvent: LogEvent) = Unit
}
