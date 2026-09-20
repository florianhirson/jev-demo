package com.florian.hirson.jevdemo.domain.evaluation

/** Port: a labeled dataset of [LabeledLogEvent]s to check classification accuracy against. */
interface ErrorsDataset {
    fun load(): List<LabeledLogEvent>
}
