package com.florian.hirson.jevdemo.domain.triage

/**
 * The result of triaging a [LogEvent]: jev's three answers, already typed as
 * domain vocabulary, each choice/score answer paired with its own
 * [Confidence] (a `noul` carries none of its own — see [Actionable.confidence]).
 */
data class Classification(
    val category: Category,
    val categoryConfidence: Confidence,
    val severity: Severity,
    val severityConfidence: Confidence,
    val actionable: Actionable,
) {
    /**
     * [AUTOMATIC][RoutingDecision.AUTOMATIC] only when every field clears its
     * own [thresholds] entry; any single low-confidence field sends the
     * whole classification [FOR_REVIEW][RoutingDecision.FOR_REVIEW] — a
     * confident category paired with an ambiguous severity is still an
     * ambiguous classification.
     */
    fun route(thresholds: RoutingThresholds): RoutingDecision =
        if (categoryConfidence >= thresholds.category &&
            severityConfidence >= thresholds.severity &&
            actionable.confidence >= thresholds.actionable
        ) {
            RoutingDecision.AUTOMATIC
        } else {
            RoutingDecision.FOR_REVIEW
        }
}
