package com.florian.hirson.jevdemo.domain.evaluation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EvaluationReportTest {

    @Test
    fun `of regroupe les resultats par tranche et compte les reussites`() {
        val report = EvaluationReport.of(
            listOf(
                TierOutcome(ConfidenceTier.VERY_HIGH, correct = true),
                TierOutcome(ConfidenceTier.VERY_HIGH, correct = true),
                TierOutcome(ConfidenceTier.VERY_HIGH, correct = false),
                TierOutcome(ConfidenceTier.LOW, correct = false),
            ),
        )

        assertEquals(
            TierAccuracy(ConfidenceTier.VERY_HIGH, correct = 2, total = 3),
            report.tierAccuracies.single { it.tier == ConfidenceTier.VERY_HIGH },
        )
        assertEquals(
            TierAccuracy(ConfidenceTier.LOW, correct = 0, total = 1),
            report.tierAccuracies.single { it.tier == ConfidenceTier.LOW },
        )
    }

    @Test
    fun `of couvre toujours les quatre tranches meme sans resultat`() {
        val report = EvaluationReport.of(emptyList())

        assertEquals(ConfidenceTier.entries.toSet(), report.tierAccuracies.map { it.tier }.toSet())
        assertEquals(4, report.tierAccuracies.size)
    }

    @Test
    fun `une tranche manquante est rejetee a la construction`() {
        assertFailsWith<IncompleteEvaluationReport> {
            EvaluationReport(listOf(TierAccuracy(ConfidenceTier.LOW, correct = 0, total = 0)))
        }
    }

    @Test
    fun `une tranche dupliquee est rejetee a la construction`() {
        assertFailsWith<IncompleteEvaluationReport> {
            EvaluationReport(
                listOf(
                    TierAccuracy(ConfidenceTier.LOW, correct = 0, total = 0),
                    TierAccuracy(ConfidenceTier.LOW, correct = 0, total = 0),
                    TierAccuracy(ConfidenceTier.MEDIUM, correct = 0, total = 0),
                    TierAccuracy(ConfidenceTier.HIGH, correct = 0, total = 0),
                ),
            )
        }
    }
}
