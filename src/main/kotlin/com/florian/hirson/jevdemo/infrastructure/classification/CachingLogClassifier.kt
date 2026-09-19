package com.florian.hirson.jevdemo.infrastructure.classification

import com.florian.hirson.jevdemo.domain.triage.Classification
import com.florian.hirson.jevdemo.domain.triage.ClassificationCache
import com.florian.hirson.jevdemo.domain.triage.Fingerprint
import com.florian.hirson.jevdemo.domain.triage.LogClassifier
import com.florian.hirson.jevdemo.domain.triage.LogEvent

/**
 * Decorates a [LogClassifier] with a [ClassificationCache] lookup by
 * [Fingerprint]: a recurring error is classified once, not once per
 * occurrence. Implements the same port it wraps, so the use case and the
 * rest of the pipeline stay unaware caching exists at all.
 */
class CachingLogClassifier(
    private val delegate: LogClassifier,
    private val cache: ClassificationCache,
) : LogClassifier {

    override fun classify(logEvent: LogEvent): Classification {
        val fingerprint = Fingerprint.of(logEvent)
        cache.get(fingerprint)?.let { return it }

        val classification = delegate.classify(logEvent)
        cache.put(fingerprint, classification)
        return classification
    }
}
