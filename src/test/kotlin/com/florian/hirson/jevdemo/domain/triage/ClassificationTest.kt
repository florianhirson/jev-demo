package com.florian.hirson.jevdemo.domain.triage

import kotlin.test.Test
import kotlin.test.assertEquals

class ClassificationTest {

    private val thresholds = RoutingThresholds(
        categoryConfidence = Confidence(0.7),
        severityConfidence = Confidence(0.7),
        actionableConfidence = Confidence(0.7),
    )

    private fun classification(categoryConfidence: Double, severityConfidence: Double, actionableProbability: Double) =
        Classification(
            category = Category.EXTERNAL_DEPENDENCY,
            categoryConfidence = Confidence(categoryConfidence),
            severity = Severity(2.0),
            severityConfidence = Confidence(severityConfidence),
            actionable = Actionable(actionableProbability),
        )

    @Test
    fun `une classification confiante sur les trois champs est routee automatiquement`() {
        // actionable=0.95 -> confidence = |0.95-0.5|*2 = 0.9, au-dessus du seuil
        val classification = classification(categoryConfidence = 0.8, severityConfidence = 0.8, actionableProbability = 0.95)

        assertEquals(RoutingDecision.AUTOMATIC, classification.route(thresholds))
    }

    @Test
    fun `une categorie peu confiante part en revue`() {
        val classification = classification(categoryConfidence = 0.5, severityConfidence = 0.8, actionableProbability = 0.95)

        assertEquals(RoutingDecision.FOR_REVIEW, classification.route(thresholds))
    }

    @Test
    fun `une severite peu confiante part en revue`() {
        val classification = classification(categoryConfidence = 0.8, severityConfidence = 0.5, actionableProbability = 0.95)

        assertEquals(RoutingDecision.FOR_REVIEW, classification.route(thresholds))
    }

    @Test
    fun `un actionable ambigu -proche de 0-5- part en revue`() {
        // actionable=0.55 -> confidence = |0.55-0.5|*2 = 0.1, tres en dessous du seuil
        val classification = classification(categoryConfidence = 0.8, severityConfidence = 0.8, actionableProbability = 0.55)

        assertEquals(RoutingDecision.FOR_REVIEW, classification.route(thresholds))
    }
}
