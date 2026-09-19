package com.florian.hirson.jevdemo.domain.triage

/**
 * The family of cause behind a [LogEvent]. Mirrors jev's `category`
 * `choice` question — a closed set of four options, so unlike the other
 * triage fields it needs no separate validation.
 */
enum class Category {
    APPLICATION_BUG,
    EXTERNAL_DEPENDENCY,
    CONFIGURATION,
    NOISE,
}
