package com.florian.hirson.jevdemo.api

import com.florian.hirson.jevdemo.application.triage.usecase.ListPendingReviewsUseCase
import com.florian.hirson.jevdemo.domain.triage.Actionable
import com.florian.hirson.jevdemo.domain.triage.Category
import com.florian.hirson.jevdemo.domain.triage.Classification
import com.florian.hirson.jevdemo.domain.triage.Confidence
import com.florian.hirson.jevdemo.domain.triage.LogEvent
import com.florian.hirson.jevdemo.domain.triage.ReviewCase
import com.florian.hirson.jevdemo.domain.triage.Severity
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant

@WebMvcTest(ReviewController::class)
class ReviewControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var listPendingReviews: ListPendingReviewsUseCase

    @Test
    fun `GET api-reviews renvoie les revues en attente avec le message redige`() {
        val reviewCase = ReviewCase(
            logEvent = LogEvent(
                message = "Timeout calling 10.0.0.1 for user alice@acme.example",
                occurredAt = Instant.parse("2026-09-19T10:15:30Z"),
            ),
            classification = Classification(
                category = Category.EXTERNAL_DEPENDENCY,
                categoryConfidence = Confidence(0.6),
                severity = Severity(0.8),
                severityConfidence = Confidence(0.5),
                actionable = Actionable(0.55),
            ),
        )
        given(listPendingReviews.execute()).willReturn(listOf(reviewCase))

        mockMvc.perform(get("/api/reviews"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].message").value("Timeout calling [IP] for user [EMAIL]"))
            .andExpect(jsonPath("$[0].category").value("EXTERNAL_DEPENDENCY"))
            .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("10.0.0.1"))))
    }
}
