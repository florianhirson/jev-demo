package com.florian.hirson.jevdemo.domain.evaluation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TierAccuracyTest {

    @Test
    fun `la precision est le ratio correct sur total`() {
        assertEquals(0.75, TierAccuracy(ConfidenceTier.HIGH, correct = 3, total = 4).accuracy)
    }

    @Test
    fun `une tranche vide a une precision nulle sans division par zero`() {
        assertEquals(0.0, TierAccuracy(ConfidenceTier.HIGH, correct = 0, total = 0).accuracy)
    }

    @Test
    fun `un total negatif est rejete`() {
        assertFailsWith<IllegalArgumentException> { TierAccuracy(ConfidenceTier.HIGH, correct = 0, total = -1) }
    }

    @Test
    fun `plus de corrects que de total est rejete`() {
        assertFailsWith<IllegalArgumentException> { TierAccuracy(ConfidenceTier.HIGH, correct = 2, total = 1) }
    }
}
