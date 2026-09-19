package com.florian.hirson.jevdemo.domain.triage

/**
 * Strips content jev should never see and that a fingerprint should never vary on:
 * email addresses, tokens/API keys, and IP addresses. The same redacted text feeds
 * both the state sent to jev ([com.florian.hirson.jevdemo.infrastructure.classification.jev.JevLogClassifier])
 * and the fingerprint used to recognize recurring errors ([Fingerprint]), so two
 * otherwise-identical errors differing only by one of these values still
 * fingerprint the same.
 */
object RedactSensitiveData {

    private val EMAIL = Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""")
    private val BEARER_TOKEN = Regex("""(?i)\bBearer\s+\S+""")
    private val JWT = Regex("""\b[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}\b""")
    private val GENERIC_TOKEN = Regex("""\b(?=[A-Za-z0-9_-]*[A-Za-z])(?=[A-Za-z0-9_-]*\d)[A-Za-z0-9_-]{20,}\b""")
    private val IPV4 = Regex("""\b(?:(?:25[0-5]|2[0-4]\d|1?\d?\d)\.){3}(?:25[0-5]|2[0-4]\d|1?\d?\d)\b""")

    fun execute(message: String): String {
        var result = message
        result = EMAIL.replace(result, "[EMAIL]")
        result = BEARER_TOKEN.replace(result, "Bearer [TOKEN]")
        result = JWT.replace(result, "[TOKEN]")
        result = GENERIC_TOKEN.replace(result, "[TOKEN]")
        result = IPV4.replace(result, "[IP]")
        return result
    }
}
