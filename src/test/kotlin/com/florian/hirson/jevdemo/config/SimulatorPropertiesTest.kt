package com.florian.hirson.jevdemo.config

import kotlin.test.Test
import kotlin.test.assertFailsWith

class SimulatorPropertiesTest {

    @Test
    fun `un delai negatif est rejete`() {
        assertFailsWith<IllegalArgumentException> { SimulatorProperties(delayMs = -1) }
    }
}
