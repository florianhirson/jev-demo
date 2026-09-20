package com.florian.hirson.jevdemo.domain.triage

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ActionableTest {

    @Test
    fun `une probabilite superieure a 1-0 est rejetee comme InvalidActionableProbability`() {
        assertFailsWith<InvalidActionableProbability> { Actionable(1.1) }
    }

    @Test
    fun `une probabilite negative est aussi rejetee comme InvalidActionableProbability`() {
        assertFailsWith<InvalidActionableProbability> { Actionable(-0.01) }
    }

    @Test
    fun `NaN n-est pas une probabilite valide`() {
        assertFailsWith<InvalidActionableProbability> { Actionable(Double.NaN) }
    }

    @Test
    fun `une probabilite plus elevee compare superieure a une probabilite plus faible`() {
        assertTrue(Actionable(0.9) > Actionable(0.1))
    }

    @Test
    fun `une probabilite de 0-5 -la plus ambigue- a une confiance nulle`() {
        assertEquals(Confidence(0.0), Actionable(0.5).confidence)
    }

    @Test
    fun `une probabilite extreme a une confiance maximale`() {
        assertEquals(Confidence(1.0), Actionable(1.0).confidence)
        assertEquals(Confidence(1.0), Actionable(0.0).confidence)
    }

    @Test
    fun `la confiance croit avec la distance a 0-5`() {
        assertEquals(Confidence(0.8), Actionable(0.9).confidence)
        assertEquals(Confidence(0.8), Actionable(0.1).confidence)
    }
}
