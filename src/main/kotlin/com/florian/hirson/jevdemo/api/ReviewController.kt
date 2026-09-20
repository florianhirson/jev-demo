package com.florian.hirson.jevdemo.api

import com.florian.hirson.jevdemo.application.triage.usecase.ListPendingReviewsUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/** Exposes the review queue built by confidence-based routing (increment 4). */
@RestController
class ReviewController(private val listPendingReviews: ListPendingReviewsUseCase) {

    @GetMapping("/api/reviews")
    fun pending(): List<ReviewCaseResponse> = listPendingReviews.execute().map(ReviewCaseResponse::from)
}
