package com.florian.hirson.jevdemo.infrastructure.cache

import com.florian.hirson.jevdemo.domain.triage.ClassificationMemory
import com.florian.hirson.jevdemo.domain.triage.ClassificationMemoryContract

class InMemoryClassificationMemoryTest : ClassificationMemoryContract() {
    override fun newMemory(): ClassificationMemory = InMemoryClassificationMemory()
}
