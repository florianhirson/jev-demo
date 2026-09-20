package com.florian.hirson.jevdemo.domain.triage

/** What happens to a [Classification]: handled automatically, or sent to a human. */
enum class RoutingDecision {
    AUTOMATIC,
    FOR_REVIEW,
}
