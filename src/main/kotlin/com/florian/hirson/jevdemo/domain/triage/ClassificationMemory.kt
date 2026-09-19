package com.florian.hirson.jevdemo.domain.triage

/**
 * Port: recognizing a [LogEvent] that jev has already classified, by its
 * [Fingerprint], so a recurring error doesn't have to be re-classified.
 */
interface ClassificationMemory {
    fun recall(fingerprint: Fingerprint): Classification?
    fun remember(fingerprint: Fingerprint, classification: Classification)
}
