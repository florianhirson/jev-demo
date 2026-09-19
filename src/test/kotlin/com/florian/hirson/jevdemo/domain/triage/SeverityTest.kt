package com.florian.hirson.jevdemo.domain.triage

import kotlin.test.Test
import kotlin.test.assertFailsWith

class SeverityTest {

    @Test
    fun `une valeur negative est rejetee comme NegativeSeverity`() {
        assertFailsWith<NegativeSeverity> { Severity(-0.1) }
    }
}
