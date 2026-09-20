package com.florian.hirson.jevdemo.application.triage.usecase

import com.florian.hirson.jevdemo.domain.triage.Classification
import com.florian.hirson.jevdemo.domain.triage.RoutingDecision

/** What [TriageLogEventUseCase.execute] produced: the [classification] and the [decision] routing it took. */
data class TriageOutcome(val classification: Classification, val decision: RoutingDecision)
