package com.florian.hirson.jevdemo.domain.triage

import kotlin.test.Test
import kotlin.test.assertFailsWith

class ActionableTest {

    @Test
    fun `une probabilite hors de 0-0 a 1-0 est rejetee comme ActionableProbabilityOutOfRange`() {
        assertFailsWith<ActionableProbabilityOutOfRange> { Actionable(1.1) }
    }

    @Test
    fun `une probabilite negative est aussi rejetee comme ActionableProbabilityOutOfRange`() {
        assertFailsWith<ActionableProbabilityOutOfRange> { Actionable(-0.01) }
    }
}
