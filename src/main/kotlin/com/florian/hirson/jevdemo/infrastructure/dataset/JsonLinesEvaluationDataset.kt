package com.florian.hirson.jevdemo.infrastructure.dataset

import com.florian.hirson.jevdemo.domain.evaluation.EvaluationDataset
import com.florian.hirson.jevdemo.domain.evaluation.LabeledLogEvent
import com.florian.hirson.jevdemo.domain.triage.Category
import com.florian.hirson.jevdemo.domain.triage.LogEvent
import org.springframework.core.io.Resource
import tools.jackson.databind.ObjectMapper
import java.time.Instant

/**
 * [EvaluationDataset] adapter reading a JSON-Lines file — one [ErrorsDatasetEntryDto]
 * per line — from [resource]. Blank lines are skipped, so the file can carry
 * blank separators between groups of examples. [LogEvent.occurredAt] is
 * stamped at load time: the dataset itself carries no timestamp, only
 * representative error content.
 */
class JsonLinesEvaluationDataset(
    private val objectMapper: ObjectMapper,
    private val resource: Resource,
) : EvaluationDataset {

    override fun labeledLogEvents(): List<LabeledLogEvent> =
        resource.inputStream.bufferedReader().useLines { lines ->
            lines
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { line -> objectMapper.readValue(line, ErrorsDatasetEntryDto::class.java).toLabeledLogEvent() }
                .toList()
        }
}

private data class ErrorsDatasetEntryDto(
    val message: String,
    val stackTrace: String? = null,
    val expectedCategory: Category,
) {
    fun toLabeledLogEvent(): LabeledLogEvent = LabeledLogEvent(
        logEvent = LogEvent(message = message, stackTrace = stackTrace, occurredAt = Instant.now()),
        expectedCategory = expectedCategory,
    )
}
