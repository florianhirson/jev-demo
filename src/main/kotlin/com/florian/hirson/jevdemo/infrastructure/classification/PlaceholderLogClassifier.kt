package com.florian.hirson.jevdemo.infrastructure.classification

import com.florian.hirson.jevdemo.domain.triage.Actionable
import com.florian.hirson.jevdemo.domain.triage.Category
import com.florian.hirson.jevdemo.domain.triage.Classification
import com.florian.hirson.jevdemo.domain.triage.LogClassifier
import com.florian.hirson.jevdemo.domain.triage.LogEvent
import com.florian.hirson.jevdemo.domain.triage.Severity

/**
 * Stands in for the real jev adapter (increment 3) so the ingestion pipeline
 * can be wired and exercised end-to-end now. Always returns the same
 * uninformative classification — it exists to prove events reach a
 * [LogClassifier], not to classify anything.
 */
class PlaceholderLogClassifier : LogClassifier {

    override fun classify(logEvent: LogEvent): Classification = Classification(
        category = Category.NOISE,
        severity = Severity(0.0),
        actionable = Actionable(0.0),
    )
}
