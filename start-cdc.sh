#!/usr/bin/env bash
set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "==> Ensuring PostgreSQL publication exists..."
docker exec demo-postgres psql -U postgres -d demo_app -c \
  "CREATE PUBLICATION dbz_publication FOR TABLE public.claims;" \
  2>/dev/null && echo "    Publication created." \
  || echo "    Publication already exists — skipping."

echo ""
echo "==> Starting Debezium Connect..."
docker compose -f "$SCRIPT_DIR/docker-compose.cdc.yml" up -d

echo ""
echo "==> Waiting for Debezium Connect to be ready..."
until curl -sf http://localhost:8083/ > /dev/null 2>&1; do
  echo "    waiting..."
  sleep 3
done
echo "    Debezium Connect is ready."

echo ""
echo "==> Removing any stale connector registration..."
curl -s -X DELETE http://localhost:8083/connectors/claims-connector > /dev/null 2>&1 \
  && echo "    Removed stale connector." \
  || echo "    No existing connector found."
sleep 2

echo ""
echo "==> Registering claims-connector..."
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -X POST \
  http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d @"$SCRIPT_DIR/docker/compose/debezium/connector-configs/claims-connector.json")

if [ "$RESPONSE" = "201" ]; then
  echo "    Registered (HTTP 201)."
else
  echo "ERROR: Failed to register connector (HTTP ${RESPONSE})"
  curl -s -X POST http://localhost:8083/connectors \
    -H "Content-Type: application/json" \
    -d @"$SCRIPT_DIR/docker/compose/debezium/connector-configs/claims-connector.json"
  exit 1
fi

echo ""
echo "==> Connector status:"
sleep 3
curl -s http://localhost:8083/connectors/claims-connector/status | python3 -m json.tool

echo ""
echo "CDC running. Debezium: http://localhost:8083"
