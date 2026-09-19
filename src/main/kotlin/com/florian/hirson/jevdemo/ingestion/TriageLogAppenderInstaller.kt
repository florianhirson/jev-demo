package com.florian.hirson.jevdemo.ingestion

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Logback configures appenders from its own context, not Spring's — so a
 * plain `logback-spring.xml` entry can't have [TriageLogAppender] injected
 * with its queue. Instead, this installs the Spring-managed appender
 * instance onto the root logger once the application context is up.
 */
@Component
class TriageLogAppenderInstaller(private val appender: TriageLogAppender) {

    @PostConstruct
    fun install() {
        appender.context = LoggerFactory.getILoggerFactory() as LoggerContext
        appender.start()
        rootLogger().addAppender(appender)
    }

    @PreDestroy
    fun uninstall() {
        rootLogger().detachAppender(appender)
        appender.stop()
    }

    private fun rootLogger() = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
}
