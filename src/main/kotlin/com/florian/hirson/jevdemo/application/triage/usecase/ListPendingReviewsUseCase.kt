package com.florian.hirson.jevdemo.application.triage.usecase

import com.florian.hirson.jevdemo.domain.triage.ReviewCase
import com.florian.hirson.jevdemo.domain.triage.ReviewQueue

/** Reads the [ReviewCase]s a human still needs to look at. */
class ListPendingReviewsUseCase(private val reviewQueue: ReviewQueue) {

    fun execute(): List<ReviewCase> = reviewQueue.pending()
}
