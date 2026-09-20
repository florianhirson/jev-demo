package com.florian.hirson.jevdemo.infrastructure.review

import com.florian.hirson.jevdemo.domain.triage.ReviewQueue
import com.florian.hirson.jevdemo.domain.triage.ReviewQueueContract

class InMemoryReviewQueueTest : ReviewQueueContract() {
    override fun newQueue(): ReviewQueue = InMemoryReviewQueue()
}
