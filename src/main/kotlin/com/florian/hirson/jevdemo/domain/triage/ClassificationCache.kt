package com.florian.hirson.jevdemo.domain.triage

/**
 * Port: recognizing a [LogEvent] that jev has already classified, by its
 * [Fingerprint], so a recurring error doesn't have to be re-classified.
 */
interface ClassificationCache {
    fun get(fingerprint: Fingerprint): Classification?
    fun put(fingerprint: Fingerprint, classification: Classification)
}
