package com.florian.hirson.jevdemo.application.triage.usecase

import com.florian.hirson.jevdemo.domain.triage.Classification
import com.florian.hirson.jevdemo.domain.triage.LogClassifier
import com.florian.hirson.jevdemo.domain.triage.LogEvent

/**
 * Orchestrates the [LogClassifier] port to triage a single [LogEvent]. The
 * insertion point for PII masking, fingerprint caching and confidence-based
 * routing in later increments — none of which touch the domain or the port.
 */
class ClassifyLogEventUseCase(private val classifier: LogClassifier) {

    fun execute(logEvent: LogEvent): Classification = classifier.classify(logEvent)
}
