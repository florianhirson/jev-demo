package com.florian.hirson.jevdemo.domain.evaluation

/** Whether one classified event, bucketed into [tier], was [correct] — one row of raw material for an [EvaluationReport]. */
data class TierOutcome(val tier: ConfidenceTier, val correct: Boolean)
