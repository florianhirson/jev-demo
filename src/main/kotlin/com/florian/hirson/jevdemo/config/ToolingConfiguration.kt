package com.florian.hirson.jevdemo.config

import com.florian.hirson.jevdemo.application.evaluation.usecase.EvaluateClassificationAccuracyUseCase
import com.florian.hirson.jevdemo.domain.evaluation.EvaluationDataset
import com.florian.hirson.jevdemo.domain.triage.LogClassifier
import com.florian.hirson.jevdemo.infrastructure.dataset.JsonLinesEvaluationDataset
import com.florian.hirson.jevdemo.tooling.EvaluationReportRunner
import com.florian.hirson.jevdemo.tooling.LogSimulatorRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import tools.jackson.databind.ObjectMapper

/**
 * Composition root for the demo tooling built in increment 6: the dataset
 * backing both the log simulator and the accuracy evaluator, and the two
 * opt-in [org.springframework.boot.CommandLineRunner]s themselves. Neither
 * runner bean exists unless its own property enables it, so the app's
 * default behavior (increments 1-5) is unaffected when this configuration is
 * present but inactive.
 */
@Configuration
@EnableConfigurationProperties(SimulatorProperties::class)
class ToolingConfiguration {

    @Bean
    fun evaluationDataset(objectMapper: ObjectMapper): EvaluationDataset =
        JsonLinesEvaluationDataset(objectMapper, ClassPathResource("data/errors.jsonl"))

    @Bean
    @ConditionalOnProperty(prefix = "simulator", name = ["enabled"], havingValue = "true")
    fun logSimulatorRunner(dataset: EvaluationDataset, properties: SimulatorProperties): LogSimulatorRunner =
        LogSimulatorRunner(dataset, properties.delayMs)

    @Bean
    @ConditionalOnProperty(prefix = "evaluation", name = ["enabled"], havingValue = "true")
    fun evaluateClassificationAccuracyUseCase(
        dataset: EvaluationDataset,
        classifier: LogClassifier,
    ): EvaluateClassificationAccuracyUseCase = EvaluateClassificationAccuracyUseCase(dataset, classifier)

    @Bean
    @ConditionalOnProperty(prefix = "evaluation", name = ["enabled"], havingValue = "true")
    fun evaluationReportRunner(evaluateAccuracy: EvaluateClassificationAccuracyUseCase): EvaluationReportRunner =
        EvaluationReportRunner(evaluateAccuracy)
}
