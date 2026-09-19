package com.florian.hirson.jevdemo.infrastructure.classification.jev

/**
 * Technical failures calling jev, translated from HTTP status codes at the
 * adapter boundary (see https://docs.typesafe.ai/api.md). Not domain errors:
 * none of these is an expected business outcome of triaging a log — they're
 * caught generically by [com.florian.hirson.jevdemo.ingestion.TriageLogConsumer]
 * like any other classification failure.
 */
sealed class JevApiException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/** 401 — the API key is missing or invalid. Never retried: retrying won't fix a bad key. */
class JevUnauthorized(cause: Throwable? = null) : JevApiException("jev rejected the API key", cause)

/** 422 — the request was malformed. Never retried: it's a bug in how we built the request. */
class JevInvalidRequest(cause: Throwable? = null) : JevApiException("jev rejected the request as invalid", cause)

/** 429 — rate limit exceeded. Retried with exponential backoff, per jev's documented guidance. */
class JevRateLimited(cause: Throwable? = null) : JevApiException("jev rate limit exceeded", cause)

/** 529 — jev is overloaded. Retried with exponential backoff, per jev's documented guidance. */
class JevOverloaded(cause: Throwable? = null) : JevApiException("jev is overloaded", cause)

/** Any other failure calling jev: an unexpected status, a network error, a timeout. */
class JevUnavailable(cause: Throwable? = null) : JevApiException("jev call failed", cause)
