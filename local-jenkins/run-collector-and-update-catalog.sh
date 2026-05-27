#!/usr/bin/env bash
set -euo pipefail

JENKINS_URL="http://localhost:8080"
SCRIPT_FILE="$(cd "$(dirname "$0")/.." && pwd)/scripts/export-jenkins-catalog.groovy"
OUTPUT_FILE="$(cd "$(dirname "$0")/.." && pwd)/gradle/jenkins.versions.toml"
MAX_WAIT=120
INTERVAL=5

echo "Waiting for Jenkins at ${JENKINS_URL} ..."
elapsed=0
while true; do
  if curl -sf -o /dev/null "${JENKINS_URL}/login"; then
    echo "Jenkins is up."
    break
  fi
  if [ "$elapsed" -ge "$MAX_WAIT" ]; then
    echo "ERROR: Jenkins did not respond within ${MAX_WAIT}s" >&2
    exit 1
  fi
  echo "  Not ready yet (${elapsed}s elapsed). Retrying in ${INTERVAL}s ..."
  sleep "$INTERVAL"
  elapsed=$((elapsed + INTERVAL))
done

echo "Running export script ..."
curl -sf \
  --data-urlencode "script@${SCRIPT_FILE}" \
  "${JENKINS_URL}/scriptText" \
  -o "${OUTPUT_FILE}"

echo "Catalog written to: ${OUTPUT_FILE}"
