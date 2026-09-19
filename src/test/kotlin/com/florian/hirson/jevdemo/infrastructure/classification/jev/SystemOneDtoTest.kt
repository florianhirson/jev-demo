package com.florian.hirson.jevdemo.infrastructure.classification.jev

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import tools.jackson.databind.ObjectMapper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Verifies the request/response DTOs against jev's own documented examples
 * (https://docs.typesafe.ai/primitives/choice.md, score.md, noul.md) — the
 * risk here is Jackson's polymorphic (de)serialization of the sealed
 * Question/Answer hierarchies, not application logic.
 */
@SpringBootTest
class SystemOneDtoTest {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `une requete choice se serialise avec le discriminant type et se deserialise sans perte`() {
        val request = SystemOneRequest(
            state = "My shoes arrived in wrong size. Can I swap them?",
            model = "jev-latest",
            questions = mapOf(
                "department" to Question.Choice(
                    instructions = "Which team should handle this?",
                    criteria = mapOf(
                        "returns" to "Exchanges, refunds, wrong or damaged items",
                        "shipping" to "Delivery status, delays, lost packages",
                        "billing" to "Charges, invoices, payment problems",
                    ),
                ),
            ),
        )

        val json = objectMapper.writeValueAsString(request)
        val roundTripped = objectMapper.readValue(json, SystemOneRequest::class.java)

        assertTrue(json.contains(""""type":"choice""""))
        assertEquals(request, roundTripped)
    }

    @Test
    fun `une reponse choice documentee se deserialise correctement`() {
        val json = """
            {
              "model": "jev-latest",
              "answers": {
                "department": {
                  "type": "choice",
                  "choice": "returns",
                  "confidence": 1.0,
                  "probabilities": { "returns": 1.0, "shipping": 0.0, "billing": 0.0 }
                }
              },
              "usage": { "input_tokens": 330, "output_tokens": 34 }
            }
        """.trimIndent()

        val response = objectMapper.readValue(json, SystemOneResponse::class.java)

        val answer = response.answers["department"] as Answer.Choice
        assertEquals("returns", answer.choice)
        assertEquals(1.0, answer.confidence)
        assertEquals(1.0, answer.probabilities["returns"])
        assertEquals(330, response.usage.inputTokens)
    }

    @Test
    fun `une reponse score documentee se deserialise correctement`() {
        val json = """
            { "type": "score", "score": 1.3, "confidence": 0.54,
              "legend": {"0": "low", "1": "medium", "2": "high"},
              "probabilities": {"0": 0.0, "1": 0.7, "2": 0.3} }
        """.trimIndent()

        val answer = objectMapper.readValue(json, Answer::class.java) as Answer.Score

        assertEquals(1.3, answer.score)
        assertEquals(0.54, answer.confidence)
        assertEquals("medium", answer.legend["1"])
        assertEquals(0.7, answer.probabilities["1"])
    }

    @Test
    fun `une reponse noul documentee se deserialise correctement`() {
        val json = """{ "type": "noul", "noul": 0.99 }"""

        val answer = objectMapper.readValue(json, Answer::class.java) as Answer.Noul

        assertEquals(0.99, answer.noul)
    }
}
