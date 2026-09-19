/**
 * Driving (inbound) adapters for triage: technical machinery that turns an
 * external trigger — a Logback ERROR event — into a call to the application
 * layer. This is this project's counterpart to the generic `interface/`
 * layer (Grace's hexagonal template can't be followed literally here:
 * `interface` is a reserved word in Kotlin).
 *
 * Nothing in this package implements a domain port — that's what
 * `infrastructure/` is for. [TriageLogAppender] and [TriageLogConsumer] are
 * the actual driving adapters; [BoundedLogQueue] and
 * [TriageLogAppenderInstaller] are supporting technical machinery for them,
 * not adapters in their own right.
 */
package com.florian.hirson.jevdemo.ingestion
