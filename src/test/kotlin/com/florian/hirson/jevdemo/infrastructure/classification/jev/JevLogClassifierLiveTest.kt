package com.florian.hirson.jevdemo.infrastructure.classification.jev

import com.florian.hirson.jevdemo.domain.triage.LogEvent
import org.junit.jupiter.api.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Calls the real jev API. Opt-in only — tagged "live", excluded from the
 * default build (see build.gradle.kts). Run with a real key:
 *
 *   JEV_API_KEY=... ./gradlew test -DincludeTags=live --tests "*JevLogClassifierLiveTest"
 *
 * The environment variable overrides the test-only dummy key from
 * src/test/resources/application.properties (OS env vars outrank
 * application.properties in Spring Boot's property precedence).
 */
@Tag("live")
@SpringBootTest
class JevLogClassifierLiveTest {

    @Autowired
    lateinit var jevLogClassifier: JevLogClassifier

    @Test
    fun `classifie un log ERROR reel via l-API jev`() {
        val logEvent = LogEvent(
            message = "Connection refused calling payment-service",
            stackTrace = "java.net.ConnectException: Connection refused",
            occurredAt = Instant.now(),
        )

        val classification = jevLogClassifier.classify(logEvent)

        // Not asserting a specific value — that's jev's judgment call, not ours.
        // Just proving the whole chain (auth, request shape, response mapping)
        // produces a valid Classification against the real API.
        assertTrue(classification.severity.value >= 0.0)
        assertTrue(classification.actionable.probability in 0.0..1.0)
    }
}
