package com.florian.hirson.jevdemo.domain.triage

/**
 * The result of triaging a [LogEvent]: jev's three answers, already typed as
 * domain vocabulary. No confidence is carried here yet — nothing needs it
 * before the confidence-routing policy (increment 4), and adding it later is
 * an additive change.
 */
data class Classification(
    val category: Category,
    val severity: Severity,
    val actionable: Actionable,
)
