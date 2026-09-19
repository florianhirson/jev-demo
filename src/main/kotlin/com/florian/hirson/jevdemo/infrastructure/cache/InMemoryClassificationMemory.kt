package com.florian.hirson.jevdemo.infrastructure.cache

import com.florian.hirson.jevdemo.domain.triage.Classification
import com.florian.hirson.jevdemo.domain.triage.ClassificationMemory
import com.florian.hirson.jevdemo.domain.triage.Fingerprint
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory [ClassificationMemory], per the project's constraints (no
 * external store). Unbounded: a demo's error diversity is small enough that
 * this doesn't need eviction, unlike the bounded ingestion queue.
 */
class InMemoryClassificationMemory : ClassificationMemory {

    private val entries = ConcurrentHashMap<Fingerprint, Classification>()

    override fun recall(fingerprint: Fingerprint): Classification? = entries[fingerprint]

    override fun remember(fingerprint: Fingerprint, classification: Classification) {
        entries[fingerprint] = classification
    }
}
