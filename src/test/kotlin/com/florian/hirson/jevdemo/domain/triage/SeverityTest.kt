package com.florian.hirson.jevdemo.domain.triage

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SeverityTest {

    @Test
    fun `une valeur negative est rejetee comme InvalidSeverity`() {
        assertFailsWith<InvalidSeverity> { Severity(-0.1) }
    }

    @Test
    fun `NaN n-est pas une position valide sur le spectre`() {
        assertFailsWith<InvalidSeverity> { Severity(Double.NaN) }
    }

    @Test
    fun `un infini n-est pas une position valide sur le spectre`() {
        assertFailsWith<InvalidSeverity> { Severity(Double.POSITIVE_INFINITY) }
    }

    @Test
    fun `une severite plus elevee compare superieure a une severite plus faible`() {
        assertTrue(Severity(0.8) > Severity(0.3))
    }
}
