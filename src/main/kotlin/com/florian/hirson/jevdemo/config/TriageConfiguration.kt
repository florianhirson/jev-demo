package com.florian.hirson.jevdemo.config

import com.florian.hirson.jevdemo.application.triage.usecase.ClassifyLogEventUseCase
import com.florian.hirson.jevdemo.domain.triage.LogClassifier
import com.florian.hirson.jevdemo.infrastructure.classification.PlaceholderLogClassifier
import com.florian.hirson.jevdemo.ingestion.BoundedLogQueue
import com.florian.hirson.jevdemo.ingestion.TriageLogAppender
import com.florian.hirson.jevdemo.ingestion.TriageLogConsumer
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Composition root for the triage slice: the only place that wires a port
 * to a concrete adapter, or a domain/application object to a Spring bean
 * lifecycle. Domain and application code never reference this class.
 */
@Configuration
@EnableConfigurationProperties(TriageIngestionProperties::class)
class TriageConfiguration {

    @Bean
    fun boundedLogQueue(properties: TriageIngestionProperties): BoundedLogQueue =
        BoundedLogQueue(properties.queueCapacity)

    @Bean
    fun triageLogAppender(queue: BoundedLogQueue): TriageLogAppender = TriageLogAppender(queue)

    @Bean
    fun logClassifier(): LogClassifier = PlaceholderLogClassifier()

    @Bean
    fun classifyLogEventUseCase(logClassifier: LogClassifier): ClassifyLogEventUseCase =
        ClassifyLogEventUseCase(logClassifier)

    @Bean
    fun triageLogConsumer(
        queue: BoundedLogQueue,
        classifyLogEvent: ClassifyLogEventUseCase,
        properties: TriageIngestionProperties,
    ): TriageLogConsumer = TriageLogConsumer(queue, classifyLogEvent, properties.consumerCount)
}
