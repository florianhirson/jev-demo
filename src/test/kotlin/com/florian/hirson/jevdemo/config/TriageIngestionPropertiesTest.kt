package com.florian.hirson.jevdemo.config

import kotlin.test.Test
import kotlin.test.assertFailsWith

class TriageIngestionPropertiesTest {

    @Test
    fun `une capacite de file non positive est rejetee`() {
        assertFailsWith<IllegalArgumentException> { TriageIngestionProperties(queueCapacity = 0, consumerCount = 4) }
    }

    @Test
    fun `un nombre de consommateurs non positif est rejete`() {
        assertFailsWith<IllegalArgumentException> { TriageIngestionProperties(queueCapacity = 1024, consumerCount = -1) }
    }
}
