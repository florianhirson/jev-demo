package com.florian.hirson.jevdemo.domain.evaluation

/** Port: labeled log events to check classification accuracy against. */
interface EvaluationDataset {
    fun labeledLogEvents(): List<LabeledLogEvent>
}
