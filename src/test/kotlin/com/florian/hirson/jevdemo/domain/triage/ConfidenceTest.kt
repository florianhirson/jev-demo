package com.florian.hirson.jevdemo.domain.triage

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ConfidenceTest {

    @Test
    fun `une valeur superieure a 1-0 est rejetee`() {
        assertFailsWith<InvalidConfidence> { Confidence(1.1) }
    }

    @Test
    fun `une valeur negative est rejetee`() {
        assertFailsWith<InvalidConfidence> { Confidence(-0.01) }
    }

    @Test
    fun `NaN n-est pas une confiance valide`() {
        assertFailsWith<InvalidConfidence> { Confidence(Double.NaN) }
    }

    @Test
    fun `une confiance plus elevee compare superieure a une confiance plus faible`() {
        assertTrue(Confidence(0.9) > Confidence(0.3))
    }
}
