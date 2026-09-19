package com.florian.hirson.jevdemo.infrastructure.classification

import com.florian.hirson.jevdemo.domain.triage.Classification
import com.florian.hirson.jevdemo.domain.triage.ClassificationMemory
import com.florian.hirson.jevdemo.domain.triage.Fingerprint
import com.florian.hirson.jevdemo.domain.triage.LogClassifier
import com.florian.hirson.jevdemo.domain.triage.LogEvent

/**
 * Decorates a [LogClassifier] with a [ClassificationMemory] lookup by
 * [Fingerprint]: a recurring error is classified once, not once per
 * occurrence. Implements the same port it wraps, so the use case and the
 * rest of the pipeline stay unaware caching exists at all.
 */
class CachingLogClassifier(
    private val delegate: LogClassifier,
    private val memory: ClassificationMemory,
) : LogClassifier {

    override fun classify(logEvent: LogEvent): Classification {
        val fingerprint = Fingerprint.of(logEvent)
        memory.recall(fingerprint)?.let { return it }

        val classification = delegate.classify(logEvent)
        memory.remember(fingerprint, classification)
        return classification
    }
}
