package com.florian.hirson.jevdemo.domain.evaluation

import com.florian.hirson.jevdemo.domain.triage.Confidence
import kotlin.test.Test
import kotlin.test.assertEquals

class ConfidenceTierTest {

    @Test
    fun `les bornes de chaque tranche sont respectees`() {
        assertEquals(ConfidenceTier.LOW, ConfidenceTier.of(Confidence(0.0)))
        assertEquals(ConfidenceTier.LOW, ConfidenceTier.of(Confidence(0.499)))
        assertEquals(ConfidenceTier.MEDIUM, ConfidenceTier.of(Confidence(0.5)))
        assertEquals(ConfidenceTier.MEDIUM, ConfidenceTier.of(Confidence(0.699)))
        assertEquals(ConfidenceTier.HIGH, ConfidenceTier.of(Confidence(0.7)))
        assertEquals(ConfidenceTier.HIGH, ConfidenceTier.of(Confidence(0.899)))
        assertEquals(ConfidenceTier.VERY_HIGH, ConfidenceTier.of(Confidence(0.9)))
        assertEquals(ConfidenceTier.VERY_HIGH, ConfidenceTier.of(Confidence(1.0)))
    }
}
