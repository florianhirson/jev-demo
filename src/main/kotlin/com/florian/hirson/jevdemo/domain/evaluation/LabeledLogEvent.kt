package com.florian.hirson.jevdemo.domain.evaluation

import com.florian.hirson.jevdemo.domain.triage.Category
import com.florian.hirson.jevdemo.domain.triage.LogEvent

/** A [LogEvent] paired with the [Category] it is expected to be classified as — ground truth for [EvaluationReport]. */
data class LabeledLogEvent(val logEvent: LogEvent, val expectedCategory: Category)
