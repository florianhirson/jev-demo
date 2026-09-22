package com.florian.hirson.jevdemo.infrastructure.presentation

import com.florian.hirson.jevdemo.application.triage.usecase.TriageOutcome
import com.florian.hirson.jevdemo.domain.triage.Actionable
import com.florian.hirson.jevdemo.domain.triage.Category
import com.florian.hirson.jevdemo.domain.triage.Classification
import com.florian.hirson.jevdemo.domain.triage.Confidence
import com.florian.hirson.jevdemo.domain.triage.LogEvent
import com.florian.hirson.jevdemo.domain.triage.RoutingDecision
import com.florian.hirson.jevdemo.domain.triage.Severity
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertEquals

class AnsiConsoleTriageOutcomePresenterTest {

    private val green = "\u001B[32m"
    private val yellow = "\u001B[33m"
    private val red = "\u001B[31m"

    private val buffer = ByteArrayOutputStream()
    private val presenter = AnsiConsoleTriageOutcomePresenter(PrintStream(buffer, true, Charsets.UTF_8))

    private fun printed() = buffer.toString(Charsets.UTF_8)

    private fun logEvent(message: String) =
        LogEvent(message = message, occurredAt = Instant.parse("2026-09-19T10:15:30Z"))

    private fun outcome(decision: RoutingDecision, category: Category = Category.EXTERNAL_DEPENDENCY) = TriageOutcome(
        Classification(
            category = category,
            categoryConfidence = Confidence(0.96),
            severity = Severity(1.8),
            severityConfidence = Confidence(0.88),
            actionable = Actionable(0.91),
        ),
        decision,
    )

    @Test
    fun `une decision automatique s-affiche en vert avec categorie et confiances`() {
        presenter.present(logEvent("Timeout calling payment gateway"), outcome(RoutingDecision.AUTOMATIC))

        val line = printed()
        assertContains(line, green)
        assertContains(line, "AUTOMATIC")
        assertContains(line, "external dependency")
        assertContains(line, "96%")
        assertContains(line, "1.8")
        assertContains(line, "Timeout calling payment gateway")
    }

    @Test
    fun `un cas envoye en revue s-affiche en jaune`() {
        presenter.present(logEvent("Something odd"), outcome(RoutingDecision.FOR_REVIEW))

        assertContains(printed(), yellow)
        assertContains(printed(), "FOR_REVIEW")
    }

    @Test
    fun `un echec de triage s-affiche en rouge`() {
        presenter.presentTriageFailure(logEvent("Something odd"))

        assertContains(printed(), red)
        assertContains(printed(), "FAILED")
    }

    @Test
    fun `le message affiche est redige et jamais le message brut`() {
        presenter.present(logEvent("Timeout calling 10.0.0.1 for alice@acme.example"), outcome(RoutingDecision.AUTOMATIC))

        assertContains(printed(), "[IP]")
        assertContains(printed(), "[EMAIL]")
        assertFalse(printed().contains("10.0.0.1"))
        assertFalse(printed().contains("alice@acme.example"))
    }

    @Test
    fun `un message long ou multiligne tient sur une seule ligne tronquee`() {
        presenter.present(logEvent("first line\nsecond line " + "x".repeat(200)), outcome(RoutingDecision.AUTOMATIC))

        assertEquals(1, printed().lines().count { it.isNotBlank() })
        assertFalse(printed().contains("x".repeat(100)))
    }
}
