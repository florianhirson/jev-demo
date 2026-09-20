package com.florian.hirson.jevdemo.tooling

import com.florian.hirson.jevdemo.domain.evaluation.EvaluationDataset
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner

/**
 * Driving adapter: replays [dataset] as real SLF4J ERROR logs, one every
 * [delayMs], so [com.florian.hirson.jevdemo.ingestion.TriageLogAppender]
 * picks them up exactly like a live application error would. Logs
 * [com.florian.hirson.jevdemo.domain.triage.LogEvent.redactedMessage] /
 * `redactedStackTrace`, not the raw fields — same PII-safety rule as every
 * other log site in the project (see [com.florian.hirson.jevdemo.ingestion.TriageLogConsumer]);
 * the bundled dataset has nothing to redact today, but a simulator that
 * skipped this would silently stop being safe the moment it's pointed at a
 * more realistic one.
 *
 * Reads [dataset] directly rather than through a use case: unlike
 * [EvaluationReportRunner], there is no business decision to orchestrate
 * here, only replay, so a use case would be a hollow pass-through — see
 * CLAUDE.md's "Écart Grace assumé" notes on increment 6. Takes [delayMs] as
 * a plain value rather than the whole
 * [com.florian.hirson.jevdemo.config.SimulatorProperties], so this
 * driving-adapter package never imports the composition-root config
 * package, which would close an import cycle with
 * [com.florian.hirson.jevdemo.config.ToolingConfiguration] (which imports
 * this one to wire it) — same reasoning as `TriageConfiguration` passing
 * `properties.consumerCount` into `TriageLogConsumer` as a plain `Int`.
 */
class LogSimulatorRunner(
    private val dataset: EvaluationDataset,
    private val delayMs: Long,
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(LogSimulatorRunner::class.java)

    override fun run(vararg args: String) {
        dataset.labeledLogEvents().forEach { labeled ->
            val logEvent = labeled.logEvent
            val stackTrace = logEvent.redactedStackTrace
            if (stackTrace != null) {
                logger.error(logEvent.redactedMessage, SimulatedFailure(stackTrace))
            } else {
                logger.error(logEvent.redactedMessage)
            }
            if (delayMs > 0) Thread.sleep(delayMs)
        }
    }
}

/** Carries [detail] so a simulated log line has a throwable to format, the way a real one would. */
private class SimulatedFailure(detail: String) : RuntimeException(detail)
