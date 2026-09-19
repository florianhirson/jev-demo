package com.florian.hirson.jevdemo.config

import com.florian.hirson.jevdemo.application.triage.usecase.ClassifyLogEventUseCase
import com.florian.hirson.jevdemo.domain.triage.ClassificationMemory
import com.florian.hirson.jevdemo.domain.triage.LogClassifier
import com.florian.hirson.jevdemo.infrastructure.cache.InMemoryClassificationMemory
import com.florian.hirson.jevdemo.infrastructure.classification.CachingLogClassifier
import com.florian.hirson.jevdemo.infrastructure.classification.jev.JevLogClassifier
import com.florian.hirson.jevdemo.ingestion.BoundedLogQueue
import com.florian.hirson.jevdemo.ingestion.TriageLogAppender
import com.florian.hirson.jevdemo.ingestion.TriageLogConsumer
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Composition root for the triage slice: the only place that associates the
 * [LogClassifier] port with a concrete adapter — [JevLogClassifier] (built in
 * [JevConfiguration]) decorated with a cache lookup. Domain and application
 * code never reference this class. [com.florian.hirson.jevdemo.ingestion.TriageLogAppenderInstaller]
 * separately attaches the appender bean built here to Logback's root
 * logger — Logback owns its own context, so that step can't happen here.
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
    fun classificationMemory(): ClassificationMemory = InMemoryClassificationMemory()

    @Bean
    fun logClassifier(jevLogClassifier: JevLogClassifier, memory: ClassificationMemory): LogClassifier =
        CachingLogClassifier(jevLogClassifier, memory)

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
