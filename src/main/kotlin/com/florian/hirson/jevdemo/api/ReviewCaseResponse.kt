package com.florian.hirson.jevdemo.api

import com.florian.hirson.jevdemo.domain.triage.ReviewCase
import java.time.Instant

/**
 * REST projection of a [ReviewCase]. Carries [message] redacted — never the
 * raw [com.florian.hirson.jevdemo.domain.triage.LogEvent.message] — since
 * this endpoint has no authentication in front of it (see CLAUDE.md).
 */
data class ReviewCaseResponse(
    val message: String,
    val occurredAt: Instant,
    val category: String,
    val categoryConfidence: Double,
    val severity: Double,
    val severityConfidence: Double,
    val actionableProbability: Double,
) {
    companion object {
        fun from(reviewCase: ReviewCase): ReviewCaseResponse = ReviewCaseResponse(
            message = reviewCase.logEvent.redactedMessage,
            occurredAt = reviewCase.logEvent.occurredAt,
            category = reviewCase.classification.category.name,
            categoryConfidence = reviewCase.classification.categoryConfidence.value,
            severity = reviewCase.classification.severity.value,
            severityConfidence = reviewCase.classification.severityConfidence.value,
            actionableProbability = reviewCase.classification.actionable.probability,
        )
    }
}
