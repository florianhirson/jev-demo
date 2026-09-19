package com.florian.hirson.jevdemo.infrastructure.classification

import com.florian.hirson.jevdemo.domain.triage.LogEvent
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class PlaceholderLogClassifierTest {

    @Test
    fun `classifie tout evenement de la meme facon en attendant l-adaptateur jev`() {
        val classifier = PlaceholderLogClassifier()
        val logEvent = LogEvent(message = "anything", occurredAt = Instant.parse("2026-09-19T10:15:30Z"))

        val classification = classifier.classify(logEvent)

        assertEquals(classification, classifier.classify(logEvent))
    }
}
