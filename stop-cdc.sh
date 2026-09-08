#!/usr/bin/env bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "==> Stopping CDC stack..."
docker compose -f "$SCRIPT_DIR/docker-compose.cdc.yml" down "$@"
