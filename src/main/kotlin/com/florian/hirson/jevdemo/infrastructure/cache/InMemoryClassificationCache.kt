package com.florian.hirson.jevdemo.infrastructure.cache

import com.florian.hirson.jevdemo.domain.triage.Classification
import com.florian.hirson.jevdemo.domain.triage.ClassificationCache
import com.florian.hirson.jevdemo.domain.triage.Fingerprint
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory [ClassificationCache], per the project's constraints (no external
 * store). Unbounded: a demo's error diversity is small enough that this
 * doesn't need eviction, unlike the bounded ingestion queue.
 */
class InMemoryClassificationCache : ClassificationCache {

    private val entries = ConcurrentHashMap<Fingerprint, Classification>()

    override fun get(fingerprint: Fingerprint): Classification? = entries[fingerprint]

    override fun put(fingerprint: Fingerprint, classification: Classification) {
        entries[fingerprint] = classification
    }
}
