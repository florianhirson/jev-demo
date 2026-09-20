package com.florian.hirson.jevdemo.acceptance.evaluation.fakes

import com.florian.hirson.jevdemo.domain.evaluation.ErrorsDataset
import com.florian.hirson.jevdemo.domain.evaluation.LabeledLogEvent

/**
 * In-memory [ErrorsDataset] test double, standing in for the real JSONL-backed
 * adapter (`infrastructure/dataset/`). Not shipped in `main` — a fixture, not
 * a production fallback.
 */
class FakeErrorsDataset(private val labeledLogEvents: List<LabeledLogEvent>) : ErrorsDataset {
    override fun load(): List<LabeledLogEvent> = labeledLogEvents
}
