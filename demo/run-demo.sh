#!/usr/bin/env bash
# Replays the bundled error dataset against the real jev API and prints, next
# to each raw ERROR line, what jev decided. Meant to be screen-recorded (see
# demo/demo.tape) but works fine in any ANSI terminal.
set -euo pipefail
cd "$(dirname "$0")/.."

if [ -f .env ]; then set -a; . ./.env; set +a; fi
: "${JEV_API_KEY:?JEV_API_KEY must be set (see .env.example)}"

exec java -jar build/libs/jev-demo-0.0.1-SNAPSHOT.jar \
  --demo.console.enabled=true \
  --simulator.enabled=true \
  --simulator.delay-ms="${DEMO_DELAY_MS:-800}" \
  --spring.main.banner-mode=off \
  --logging.level.root=ERROR \
  '--logging.pattern.console=%clr(%d{HH:mm:ss}){faint} %clr(ERROR){red} %m%nopex%n'
