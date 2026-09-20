package com.florian.hirson.jevdemo.acceptance.evaluation.fakes

import com.florian.hirson.jevdemo.domain.evaluation.EvaluationDataset
import com.florian.hirson.jevdemo.domain.evaluation.LabeledLogEvent

/**
 * In-memory [EvaluationDataset] test double, standing in for the real
 * JSONL-backed adapter (`infrastructure/dataset/`). Not shipped in `main` —
 * a fixture, not a production fallback. Shared across acceptance and
 * `tooling/` tests, the same way `acceptance/triage/fakes/FakeLogClassifier`
 * already is.
 */
class FakeEvaluationDataset(private val entries: List<LabeledLogEvent>) : EvaluationDataset {
    override fun labeledLogEvents(): List<LabeledLogEvent> = entries
}
