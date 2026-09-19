package com.florian.hirson.jevdemo.infrastructure.cache

import com.florian.hirson.jevdemo.domain.triage.ClassificationCache
import com.florian.hirson.jevdemo.domain.triage.ClassificationCacheContract

class InMemoryClassificationCacheTest : ClassificationCacheContract() {
    override fun newCache(): ClassificationCache = InMemoryClassificationCache()
}
