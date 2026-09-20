package com.florian.hirson.jevdemo.infrastructure.review

import com.florian.hirson.jevdemo.domain.triage.ReviewCase
import com.florian.hirson.jevdemo.domain.triage.ReviewQueue
import java.util.concurrent.CopyOnWriteArrayList

/** In-memory [ReviewQueue], per the project's constraints (no external store). */
class InMemoryReviewQueue : ReviewQueue {

    private val cases = CopyOnWriteArrayList<ReviewCase>()

    override fun enqueue(reviewCase: ReviewCase) {
        cases.add(reviewCase)
    }

    override fun pending(): List<ReviewCase> = cases.toList()
}
