package com.florian.hirson.jevdemo.domain.triage

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
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
    fun `une probabilite au-dessus de la moitie est consideree probablement actionnable`() {
        assertTrue(Actionable(0.9).isLikelyActionable)
    }

    @Test
    fun `une probabilite en-dessous de la moitie n-est pas consideree probablement actionnable`() {
        assertFalse(Actionable(0.1).isLikelyActionable)
    }
}
