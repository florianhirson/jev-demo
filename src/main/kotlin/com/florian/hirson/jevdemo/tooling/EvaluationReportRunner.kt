package com.florian.hirson.jevdemo.tooling

import com.florian.hirson.jevdemo.application.evaluation.EvaluateClassificationAccuracyUseCase
import com.florian.hirson.jevdemo.domain.evaluation.EvaluationReport
import com.florian.hirson.jevdemo.domain.evaluation.TierAccuracy
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner

/**
 * Driving adapter: runs [evaluateAccuracy] against the real classifier once
 * at startup and logs the resulting [EvaluationReport] — a human-readable
 * summary, not a machine-parsed event, so it's logged as one message rather
 * than one structured log line per tier. Only wired when
 * `evaluation.enabled=true` (see [com.florian.hirson.jevdemo.config.ToolingConfiguration]);
 * calls the real jev API, so it needs `JEV_API_KEY` set.
 */
class EvaluationReportRunner(private val evaluateAccuracy: EvaluateClassificationAccuracyUseCase) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(EvaluationReportRunner::class.java)

    override fun run(vararg args: String) {
        logger.info(formatted(evaluateAccuracy.execute()))
    }

    private fun formatted(report: EvaluationReport): String =
        report.tierAccuracies.joinToString(prefix = "Confidence-tier evaluation report:\n", separator = "\n", postfix = "\n") { line(it) }

    private fun line(tierAccuracy: TierAccuracy): String =
        "  %-10s n=%-3d accuracy=%.1f%%".format(tierAccuracy.tier, tierAccuracy.total, tierAccuracy.accuracy * 100)
}
