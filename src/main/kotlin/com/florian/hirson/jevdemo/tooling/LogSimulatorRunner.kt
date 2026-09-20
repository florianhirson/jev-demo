package com.florian.hirson.jevdemo.tooling

import com.florian.hirson.jevdemo.config.SimulatorProperties
import com.florian.hirson.jevdemo.domain.evaluation.ErrorsDataset
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner

/**
 * Driving adapter: replays [dataset] as real SLF4J ERROR logs, one every
 * [SimulatorProperties.delayMs], so [com.florian.hirson.jevdemo.ingestion.TriageLogAppender]
 * picks them up exactly like a live application error would — this class
 * knows nothing about the triage pipeline it feeds. Only wired when
 * `simulator.enabled=true` (see [com.florian.hirson.jevdemo.config.ToolingConfiguration]);
 * the app's normal behavior is unaffected when it's off.
 */
class LogSimulatorRunner(
    private val dataset: ErrorsDataset,
    private val properties: SimulatorProperties,
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(LogSimulatorRunner::class.java)

    override fun run(vararg args: String) {
        dataset.load().forEach { labeled ->
            val logEvent = labeled.logEvent
            val stackTrace = logEvent.stackTrace
            if (stackTrace != null) {
                logger.error(logEvent.message, SimulatedFailure(stackTrace))
            } else {
                logger.error(logEvent.message)
            }
            if (properties.delayMs > 0) Thread.sleep(properties.delayMs)
        }
    }
}

/** Carries [detail] so a simulated log line has a throwable to format, the way a real one would. */
private class SimulatedFailure(detail: String) : RuntimeException(detail)
