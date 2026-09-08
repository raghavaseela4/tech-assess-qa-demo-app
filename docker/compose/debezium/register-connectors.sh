#!/bin/bash
# Register Debezium connectors after Connect is ready
# Usage: ./register-connectors.sh [debezium_host]

DEBEZIUM_HOST="${1:-localhost:8083}"
MAX_RETRIES=30
RETRY_INTERVAL=5

echo "Waiting for Debezium Connect at ${DEBEZIUM_HOST}..."

retries=0
until curl -sf "http://${DEBEZIUM_HOST}/"; do
  retries=$((retries + 1))
  if [ "$retries" -ge "$MAX_RETRIES" ]; then
    echo "ERROR: Debezium Connect not ready after $((MAX_RETRIES * RETRY_INTERVAL))s"
    exit 1
  fi
  echo "  Attempt ${retries}/${MAX_RETRIES} - waiting ${RETRY_INTERVAL}s..."
  sleep "$RETRY_INTERVAL"
done

echo "Debezium Connect is ready."

# Register claims-connector
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
CONFIG_FILE="${SCRIPT_DIR}/connector-configs/claims-connector.json"

if [ ! -f "$CONFIG_FILE" ]; then
  echo "ERROR: Connector config not found: ${CONFIG_FILE}"
  exit 1
fi

echo "Registering claims-connector..."
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -X POST \
  "http://${DEBEZIUM_HOST}/connectors" \
  -H "Content-Type: application/json" \
  -d @"${CONFIG_FILE}")

if [ "$RESPONSE" = "201" ] || [ "$RESPONSE" = "409" ]; then
  echo "claims-connector registered (HTTP ${RESPONSE})"
else
  echo "ERROR: Failed to register claims-connector (HTTP ${RESPONSE})"
  curl -s "http://${DEBEZIUM_HOST}/connectors" | python3 -m json.tool 2>/dev/null
  exit 1
fi

# Verify connector status
echo ""
echo "Connector status:"
curl -s "http://${DEBEZIUM_HOST}/connectors/claims-connector/status" | python3 -m json.tool 2>/dev/null || \
  curl -s "http://${DEBEZIUM_HOST}/connectors/claims-connector/status"

echo ""
echo "Debezium connector registration complete."
