package com.florian.hirson.jevdemo.acceptance.triage.fakes

import com.florian.hirson.jevdemo.domain.triage.Classification
import com.florian.hirson.jevdemo.domain.triage.LogClassifier
import com.florian.hirson.jevdemo.domain.triage.LogEvent

/**
 * In-memory [LogClassifier] test double, standing in for the real jev adapter
 * (introduced in increment 3). Not shipped in `main` — this is a fixture, not
 * a production fallback.
 */
class FakeLogClassifier(
    private val responses: Map<LogEvent, Classification>,
) : LogClassifier {

    override fun classify(logEvent: LogEvent): Classification =
        responses[logEvent]
            ?: error("FakeLogClassifier has no stubbed response for $logEvent")
}
