package com.florian.hirson.jevdemo.acceptance.evaluation

import com.florian.hirson.jevdemo.acceptance.evaluation.fakes.FakeErrorsDataset
import com.florian.hirson.jevdemo.acceptance.triage.fakes.FakeLogClassifier
import com.florian.hirson.jevdemo.application.evaluation.EvaluateClassificationAccuracyUseCase
import com.florian.hirson.jevdemo.domain.evaluation.ConfidenceTier
import com.florian.hirson.jevdemo.domain.evaluation.LabeledLogEvent
import com.florian.hirson.jevdemo.domain.evaluation.TierAccuracy
import com.florian.hirson.jevdemo.domain.triage.Actionable
import com.florian.hirson.jevdemo.domain.triage.Category
import com.florian.hirson.jevdemo.domain.triage.Classification
import com.florian.hirson.jevdemo.domain.triage.Confidence
import com.florian.hirson.jevdemo.domain.triage.LogEvent
import com.florian.hirson.jevdemo.domain.triage.Severity
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class EvaluateClassificationAccuracyAcceptanceTest {

    private fun logEvent(message: String) =
        LogEvent(message = message, occurredAt = Instant.parse("2026-09-20T10:00:00Z"))

    private fun classification(category: Category, categoryConfidence: Double) = Classification(
        category = category,
        categoryConfidence = Confidence(categoryConfidence),
        severity = Severity(0.5),
        severityConfidence = Confidence(0.9),
        actionable = Actionable(0.8),
    )

    @Test
    fun `le rapport regroupe la precision de categorie par tranche de confiance`() {
        // Given trois exemples étiquetés, classés avec des confidences de catégorie dans des tranches différentes
        val correctVeryHigh = logEvent("Connection refused calling payment-service")
        val wrongVeryHigh = logEvent("NullPointerException in OrderService")
        val correctLow = logEvent("Unexpected token in config.yaml")

        val dataset = FakeErrorsDataset(
            listOf(
                LabeledLogEvent(correctVeryHigh, expectedCategory = Category.EXTERNAL_DEPENDENCY),
                LabeledLogEvent(wrongVeryHigh, expectedCategory = Category.APPLICATION_BUG),
                LabeledLogEvent(correctLow, expectedCategory = Category.CONFIGURATION),
            ),
        )
        val classifier = FakeLogClassifier(
            mapOf(
                correctVeryHigh to classification(Category.EXTERNAL_DEPENDENCY, categoryConfidence = 0.95),
                wrongVeryHigh to classification(Category.CONFIGURATION, categoryConfidence = 0.92),
                correctLow to classification(Category.CONFIGURATION, categoryConfidence = 0.4),
            ),
        )
        val evaluateAccuracy = EvaluateClassificationAccuracyUseCase(dataset, classifier)

        // When le rapport de précision par tranche de confiance est généré
        val report = evaluateAccuracy.execute()

        // Then chaque tranche concernée affiche son nombre d'exemples corrects sur son total
        assertEquals(
            TierAccuracy(ConfidenceTier.LOW, correct = 1, total = 1),
            report.tierAccuracies.single { it.tier == ConfidenceTier.LOW },
        )
        assertEquals(
            TierAccuracy(ConfidenceTier.VERY_HIGH, correct = 1, total = 2),
            report.tierAccuracies.single { it.tier == ConfidenceTier.VERY_HIGH },
        )
        assertEquals(
            TierAccuracy(ConfidenceTier.MEDIUM, correct = 0, total = 0),
            report.tierAccuracies.single { it.tier == ConfidenceTier.MEDIUM },
        )
        assertEquals(
            TierAccuracy(ConfidenceTier.HIGH, correct = 0, total = 0),
            report.tierAccuracies.single { it.tier == ConfidenceTier.HIGH },
        )
    }
}
