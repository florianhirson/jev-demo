package com.florian.hirson.jevdemo.config

import com.florian.hirson.jevdemo.application.triage.usecase.ListPendingReviewsUseCase
import com.florian.hirson.jevdemo.application.triage.usecase.TriageLogEventUseCase
import com.florian.hirson.jevdemo.application.triage.usecase.TriageOutcomePresenter
import com.florian.hirson.jevdemo.domain.triage.ClassificationMemory
import com.florian.hirson.jevdemo.domain.triage.Confidence
import com.florian.hirson.jevdemo.domain.triage.LogClassifier
import com.florian.hirson.jevdemo.domain.triage.ReviewQueue
import com.florian.hirson.jevdemo.domain.triage.RoutingThresholds
import com.florian.hirson.jevdemo.domain.triage.TriageMetrics
import com.florian.hirson.jevdemo.infrastructure.cache.InMemoryClassificationMemory
import com.florian.hirson.jevdemo.infrastructure.classification.CachingLogClassifier
import com.florian.hirson.jevdemo.infrastructure.classification.jev.JevLogClassifier
import com.florian.hirson.jevdemo.infrastructure.observability.MicrometerTriageMetrics
import com.florian.hirson.jevdemo.infrastructure.presentation.AnsiConsoleTriageOutcomePresenter
import com.florian.hirson.jevdemo.infrastructure.presentation.SilentTriageOutcomePresenter
import com.florian.hirson.jevdemo.infrastructure.review.InMemoryReviewQueue
import com.florian.hirson.jevdemo.ingestion.BoundedLogQueue
import com.florian.hirson.jevdemo.ingestion.TriageLogAppender
import com.florian.hirson.jevdemo.ingestion.TriageLogConsumer
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
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
@EnableConfigurationProperties(value = [TriageIngestionProperties::class, TriageRoutingProperties::class])
class TriageConfiguration {

    @Bean
    fun boundedLogQueue(properties: TriageIngestionProperties): BoundedLogQueue =
        BoundedLogQueue(properties.queueCapacity)

    @Bean
    fun triageLogAppender(queue: BoundedLogQueue): TriageLogAppender = TriageLogAppender(queue)

    @Bean
    fun classificationMemory(): ClassificationMemory = InMemoryClassificationMemory()

    @Bean
    fun logClassifier(
        @Qualifier("jevLogClassifier") jevLogClassifier: JevLogClassifier,
        memory: ClassificationMemory,
    ): LogClassifier = CachingLogClassifier(jevLogClassifier, memory)

    @Bean
    fun reviewQueue(): ReviewQueue = InMemoryReviewQueue()

    @Bean
    fun triageMetrics(registry: MeterRegistry): TriageMetrics = MicrometerTriageMetrics(registry)

    @Bean
    fun routingThresholds(properties: TriageRoutingProperties): RoutingThresholds = RoutingThresholds(
        categoryConfidence = Confidence(properties.categoryThreshold),
        severityConfidence = Confidence(properties.severityThreshold),
        actionableConfidence = Confidence(properties.actionableThreshold),
    )

    @Bean
    fun triageLogEventUseCase(
        logClassifier: LogClassifier,
        reviewQueue: ReviewQueue,
        thresholds: RoutingThresholds,
        metrics: TriageMetrics,
    ): TriageLogEventUseCase = TriageLogEventUseCase(logClassifier, reviewQueue, thresholds, metrics)

    @Bean
    fun listPendingReviewsUseCase(reviewQueue: ReviewQueue): ListPendingReviewsUseCase =
        ListPendingReviewsUseCase(reviewQueue)

    @Bean
    fun triageOutcomePresenter(@Value("\${demo.console.enabled:false}") consoleEnabled: Boolean): TriageOutcomePresenter =
        if (consoleEnabled) AnsiConsoleTriageOutcomePresenter() else SilentTriageOutcomePresenter

    @Bean
    fun triageLogConsumer(
        queue: BoundedLogQueue,
        triageLogEvent: TriageLogEventUseCase,
        metrics: TriageMetrics,
        properties: TriageIngestionProperties,
        presenter: TriageOutcomePresenter,
    ): TriageLogConsumer =
        TriageLogConsumer(queue, triageLogEvent, metrics, properties.consumerCount, presenter)
}
