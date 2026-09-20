package com.florian.hirson.jevdemo.infrastructure.dataset

import com.florian.hirson.jevdemo.domain.triage.Category
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.ClassPathResource
import tools.jackson.databind.ObjectMapper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Uses the real Spring-managed [ObjectMapper] (like [com.florian.hirson.jevdemo.infrastructure.classification.jev.SystemOneDtoTest]):
 * the risk here is Jackson's handling of the DTO's optional [stackTrace],
 * not application logic.
 */
@SpringBootTest
class JsonLinesEvaluationDatasetTest {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    private fun datasetOf(jsonl: String) =
        JsonLinesEvaluationDataset(objectMapper, ByteArrayResource(jsonl.trimIndent().toByteArray()))

    @Test
    fun `chaque ligne JSON devient un evenement etiquete`() {
        val jsonl = """
            {"message":"Connection refused calling payment-service","expectedCategory":"EXTERNAL_DEPENDENCY"}
            {"message":"NullPointerException at OrderService.java:42","stackTrace":"java.lang.NullPointerException","expectedCategory":"APPLICATION_BUG"}
        """

        val labeled = datasetOf(jsonl).labeledLogEvents()

        assertEquals(2, labeled.size)
        assertEquals("Connection refused calling payment-service", labeled[0].logEvent.message)
        assertNull(labeled[0].logEvent.stackTrace)
        assertEquals(Category.EXTERNAL_DEPENDENCY, labeled[0].expectedCategory)
        assertEquals("java.lang.NullPointerException", labeled[1].logEvent.stackTrace)
        assertEquals(Category.APPLICATION_BUG, labeled[1].expectedCategory)
    }

    @Test
    fun `les lignes vides sont ignorees`() {
        val jsonl = """
            {"message":"a","expectedCategory":"NOISE"}

            {"message":"b","expectedCategory":"NOISE"}
        """

        assertEquals(2, datasetOf(jsonl).labeledLogEvents().size)
    }

    @Test
    fun `le jeu de donnees livre en main se charge et couvre les quatre categories`() {
        val labeled = JsonLinesEvaluationDataset(objectMapper, ClassPathResource("data/errors.jsonl")).labeledLogEvents()

        assertTrue(labeled.isNotEmpty())
        assertEquals(Category.entries.toSet(), labeled.map { it.expectedCategory }.toSet())
    }
}
