# jev-demo

A demo of streaming ERROR-log triage classified by **jev** (TypeSafe AI's
"System One model" — early access, [docs](https://docs.typesafe.ai/llms.txt)),
built behind a hexagonal architecture so jev is just one replaceable adapter
behind a `LogClassifier` port. Written for a GitHub repo and a LinkedIn
write-up: proportioned to a demo, not a product.

## What it does

Any `ERROR` log emitted anywhere in the app is picked up by a custom Logback
appender, queued, and classified by jev along three axes — `category`
(application bug / external dependency / configuration / noise), `severity`,
and `actionable` — each with its own confidence. A per-field confidence
threshold decides whether the result is trusted automatically or routed to a
review queue (`GET /api/reviews`). Every classification is recorded as a
Prometheus metric.

## Running it

```bash
cp .env.example .env   # fill in JEV_API_KEY
export $(cat .env | xargs)
./gradlew bootRun
```

The app needs `JEV_API_KEY` to start (the classifier adapter is wired
unconditionally) — see [docs.typesafe.ai](https://docs.typesafe.ai/llms.txt)
for how to get one. Tests never need it: they run against fakes by default,
and the handful that call the real jev API are tagged `live` and excluded
unless run explicitly:

```bash
./gradlew test                          # default: fakes only
JEV_API_KEY=... ./gradlew test -DincludeTags=live
```

## Seeing it triage something

The app has no built-in error source of its own — it triages whatever ERROR
logs the JVM it's running in happens to emit. Two ways to give it some:

- **Trigger a real one**: anything that logs at ERROR anywhere in
  `com.florian.hirson.jevdemo` (outside the triage machinery itself) gets
  picked up.
- **Replay the bundled dataset**: `src/main/resources/data/errors.jsonl` has
  24 representative error lines across all four categories. Enable the log
  simulator to replay them as real ERROR logs, one every 200ms by default:

  ```bash
  JEV_API_KEY=... ./gradlew bootRun --args='--simulator.enabled=true'
  ```

  Watch `GET /api/reviews` fill up, and `/actuator/prometheus` count
  `triage_routed_total` and `triage_failed_total`.

## Checking whether confidence actually predicts accuracy

The routing policy (`triage.routing.*`) trusts a classification automatically
only above a confidence threshold — which only makes sense if higher
confidence really does mean higher accuracy. The same bundled dataset carries
an expected category per entry, so it doubles as an evaluation set:

```bash
JEV_API_KEY=... ./gradlew bootRun --args='--evaluation.enabled=true'
```

This classifies every dataset entry directly (no ingestion pipeline
involved), buckets the results into four confidence tiers (`LOW` <0.5,
`MEDIUM` <0.7, `HIGH` <0.9, `VERY_HIGH` ≥0.9), and logs category accuracy per
tier once at startup — the report the default 0.7 thresholds
(`TriageRoutingProperties`) are still a starting guess against, pending real
calibration.

## Observability

`/actuator/health` and `/actuator/prometheus` are exposed by default. An
optional local Prometheus + Grafana stack is included for exploring the
metrics visually — not part of any deployment, just a demo convenience:

```bash
cd deploy && docker compose up
```

Then open Prometheus at `localhost:9090` and Grafana at `localhost:3000`
(anonymous admin — see `deploy/docker-compose.yml` for why that's fine for a
local-only stack).

## Architecture

Hexagonal, strict dependency rule (everything points inward to `domain/`):

```
domain/triage/          LogEvent, Classification, RoutingThresholds, ports (LogClassifier, ReviewQueue, TriageMetrics, ...)
domain/evaluation/      LabeledLogEvent, ConfidenceTier, EvaluationReport, ErrorsDataset (port)
application/            use cases orchestrating the domain through ports
infrastructure/         adapters: jev over HTTP, in-memory stores, Micrometer, the JSONL dataset reader
ingestion/, api/,       driving adapters: they trigger a use case call, they implement no port —
  tooling/              see CLAUDE.md's "Écart Grace assumé" section for why they sit outside infrastructure/
config/                 composition roots: the only place a port meets its concrete adapter
```

See `CLAUDE.md` (not published — internal working notes) for the detailed,
increment-by-increment design rationale.

## Increments

1. Skeleton, domain, `LogClassifier` port + acceptance test with a fake
   classifier.
2. Ingestion: Logback appender → bounded queue → virtual-thread consumers.
3. Real jev adapter (`@HttpExchange`, sealed DTOs) + PII masking + fingerprint
   cache.
4. Confidence-threshold routing policy + in-memory review queue + REST
   endpoint.
5. Observability: `TriageMetrics` port + Micrometer/Prometheus adapter,
   Actuator exposure.
6. `errors.jsonl` dataset + log simulator + confidence-tier accuracy
   evaluation + this README + optional Prometheus/Grafana compose stack.
