package com.florian.hirson.jevdemo.infrastructure.presentation

import com.florian.hirson.jevdemo.application.triage.usecase.TriageOutcome
import com.florian.hirson.jevdemo.application.triage.usecase.TriageOutcomePresenter
import com.florian.hirson.jevdemo.domain.triage.Category
import com.florian.hirson.jevdemo.domain.triage.LogEvent
import com.florian.hirson.jevdemo.domain.triage.RoutingDecision
import java.io.PrintStream
import java.util.Locale

/**
 * [TriageOutcomePresenter] for a terminal: one coloured line per triaged
 * event, meant to be watched (or screen-recorded) next to the raw log stream.
 * Shows [LogEvent.redactedMessage], never the raw message — same PII rule as
 * every other output of the project. One `println` per event keeps lines from
 * interleaving when several consumers present at once.
 */
class AnsiConsoleTriageOutcomePresenter(private val out: PrintStream = System.out) : TriageOutcomePresenter {

    override fun present(logEvent: LogEvent, outcome: TriageOutcome) {
        val classification = outcome.classification
        val badge = when (outcome.decision) {
            RoutingDecision.AUTOMATIC -> BOLD + GREEN + "✔ AUTOMATIC  "
            RoutingDecision.FOR_REVIEW -> BOLD + YELLOW + "⚠ FOR_REVIEW "
        }
        val category = classification.category.let { colorOf(it) + it.name.lowercase().replace('_', ' ').padEnd(19) }
        val fields = "cat %3d%%  sev %.1f (%d%%)  act p=%d%%".format(
            Locale.ROOT,
            percent(classification.categoryConfidence.value),
            classification.severity.value,
            percent(classification.severityConfidence.value),
            percent(classification.actionable.probability),
        )
        out.println("$badge$RESET $category$RESET $DIM$fields$RESET  ${summary(logEvent)}")
    }

    override fun presentTriageFailure(logEvent: LogEvent) {
        out.println("$BOLD$RED✖ FAILED     $RESET ${DIM}triage did not complete$RESET  ${summary(logEvent)}")
    }

    private fun summary(logEvent: LogEvent): String =
        logEvent.redactedMessage.lineSequence().first().let { if (it.length > MAX_MESSAGE) it.take(MAX_MESSAGE - 1) + "…" else it }

    private fun percent(fraction: Double): Int = Math.round(fraction * 100).toInt()

    private fun colorOf(category: Category): String = when (category) {
        Category.APPLICATION_BUG -> RED
        Category.EXTERNAL_DEPENDENCY -> MAGENTA
        Category.CONFIGURATION -> CYAN
        Category.NOISE -> DIM
    }

    private companion object {
        const val MAX_MESSAGE = 70
        const val RESET = "\u001B[0m"
        const val BOLD = "\u001B[1m"
        const val DIM = "\u001B[2m"
        const val RED = "\u001B[31m"
        const val GREEN = "\u001B[32m"
        const val YELLOW = "\u001B[33m"
        const val MAGENTA = "\u001B[35m"
        const val CYAN = "\u001B[36m"
    }
}
