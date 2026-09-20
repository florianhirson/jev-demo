package com.florian.hirson.jevdemo.tooling

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.florian.hirson.jevdemo.acceptance.evaluation.fakes.FakeEvaluationDataset
import com.florian.hirson.jevdemo.acceptance.triage.fakes.FakeLogClassifier
import com.florian.hirson.jevdemo.application.evaluation.usecase.EvaluateClassificationAccuracyUseCase
import com.florian.hirson.jevdemo.domain.evaluation.LabeledLogEvent
import com.florian.hirson.jevdemo.domain.triage.Actionable
import com.florian.hirson.jevdemo.domain.triage.Category
import com.florian.hirson.jevdemo.domain.triage.Classification
import com.florian.hirson.jevdemo.domain.triage.Confidence
import com.florian.hirson.jevdemo.domain.triage.LogEvent
import com.florian.hirson.jevdemo.domain.triage.Severity
import org.slf4j.LoggerFactory
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertTrue

class EvaluationReportRunnerTest {

    @Test
    fun `le rapport d-evaluation est journalise avec ses tranches de confiance`() {
        val logEvent = LogEvent(
            message = "Connection refused calling payment-service",
            occurredAt = Instant.parse("2026-09-20T10:00:00Z"),
        )
        val dataset = FakeEvaluationDataset(listOf(LabeledLogEvent(logEvent, Category.EXTERNAL_DEPENDENCY)))
        val classification = Classification(
            category = Category.EXTERNAL_DEPENDENCY,
            categoryConfidence = Confidence(0.95),
            severity = Severity(0.5),
            severityConfidence = Confidence(0.9),
            actionable = Actionable(0.8),
        )
        val evaluateAccuracy = EvaluateClassificationAccuracyUseCase(dataset, FakeLogClassifier(mapOf(logEvent to classification)))

        val logbackLogger = LoggerFactory.getLogger(EvaluationReportRunner::class.java) as Logger
        val appender = ListAppender<ILoggingEvent>()
        appender.start()
        logbackLogger.addAppender(appender)

        try {
            EvaluationReportRunner(evaluateAccuracy).run()
        } finally {
            logbackLogger.detachAppender(appender)
        }

        val message = appender.list.single().formattedMessage
        assertTrue(message.contains("VERY_HIGH"))
        assertTrue(message.contains("LOW"))
    }
}
