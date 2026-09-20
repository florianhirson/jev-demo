package com.florian.hirson.jevdemo.domain.triage

/** Port: holding [ReviewCase]s that confidence-based routing sent to a human. */
interface ReviewQueue {
    fun submit(reviewCase: ReviewCase)
    fun pending(): List<ReviewCase>
}
