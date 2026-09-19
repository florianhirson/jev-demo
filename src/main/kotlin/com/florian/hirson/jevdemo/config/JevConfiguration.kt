package com.florian.hirson.jevdemo.config

import com.florian.hirson.jevdemo.infrastructure.classification.jev.JevApiException
import com.florian.hirson.jevdemo.infrastructure.classification.jev.JevLogClassifier
import com.florian.hirson.jevdemo.infrastructure.classification.jev.JevProperties
import com.florian.hirson.jevdemo.infrastructure.classification.jev.SystemOneClient
import com.florian.hirson.jevdemo.infrastructure.classification.jev.isTransient
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.core.IntervalFunction
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import java.net.http.HttpClient

/**
 * Composition root for the jev adapter: the HTTP client, the resilience
 * policy, and the concrete [JevLogClassifier] wiring them together. Kept
 * separate from [TriageConfiguration], which only associates the resulting
 * adapter with the [com.florian.hirson.jevdemo.domain.triage.LogClassifier]
 * port — this class knows about jev, that one doesn't.
 */
@Configuration
@EnableConfigurationProperties(JevProperties::class)
class JevConfiguration {

    @Bean
    fun jevRestClient(properties: JevProperties, @Value("\${JEV_API_KEY}") apiKey: String): RestClient {
        val httpClient = HttpClient.newBuilder()
            .connectTimeout(properties.connectTimeout)
            .build()
        val requestFactory = JdkClientHttpRequestFactory(httpClient)
        requestFactory.setReadTimeout(properties.readTimeout)

        return RestClient.builder()
            .baseUrl(properties.baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $apiKey")
            .requestFactory(requestFactory)
            .build()
    }

    @Bean
    fun systemOneClient(restClient: RestClient): SystemOneClient {
        val adapter = RestClientAdapter.create(restClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()
        return factory.createClient(SystemOneClient::class.java)
    }

    @Bean
    fun jevCircuitBreaker(properties: JevProperties): CircuitBreaker {
        val config = CircuitBreakerConfig.custom()
            .failureRateThreshold(properties.circuitBreakerFailureRateThreshold)
            .waitDurationInOpenState(properties.circuitBreakerWaitDurationInOpenState)
            .build()
        return CircuitBreaker.of("jev", config)
    }

    @Bean
    fun jevRetry(properties: JevProperties): Retry {
        val config = RetryConfig.custom<Any>()
            .maxAttempts(properties.maxAttempts)
            .intervalFunction(IntervalFunction.ofExponentialRandomBackoff())
            .retryOnException { it is JevApiException && it.isTransient() }
            .build()
        return Retry.of("jev", config)
    }

    @Bean
    fun jevLogClassifier(
        client: SystemOneClient,
        properties: JevProperties,
        circuitBreaker: CircuitBreaker,
        retry: Retry,
    ): JevLogClassifier = JevLogClassifier(client, properties, circuitBreaker, retry)
}
