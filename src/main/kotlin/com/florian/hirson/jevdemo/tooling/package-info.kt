/**
 * Driving adapters for the demo tooling built in increment 6:
 * [com.florian.hirson.jevdemo.tooling.LogSimulatorRunner] replays the dataset
 * as real ERROR logs through the ingestion pipeline (increments 1-2), and
 * [com.florian.hirson.jevdemo.tooling.EvaluationReportRunner] classifies it
 * directly to report accuracy by confidence tier. Both are opt-in
 * [org.springframework.boot.CommandLineRunner]s, wired only when their own
 * property enables them — see [com.florian.hirson.jevdemo.config.ToolingConfiguration].
 *
 * Like [com.florian.hirson.jevdemo.ingestion] and [com.florian.hirson.jevdemo.api],
 * this package holds driving adapters outside `infrastructure/`: they trigger
 * a call, they implement no port. See CLAUDE.md's "Écart Grace assumé" section.
 */
package com.florian.hirson.jevdemo.tooling
