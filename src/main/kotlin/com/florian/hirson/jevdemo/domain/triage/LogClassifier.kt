package com.florian.hirson.jevdemo.domain.triage

/**
 * Port: the ability to classify a [LogEvent]. jev is the adapter that will
 * implement this from increment 3 onward — nothing in the domain or the use
 * case depends on jev directly.
 */
interface LogClassifier {
    fun classify(logEvent: LogEvent): Classification
}
