#!/usr/bin/env bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "==> Stopping infrastructure..."
docker compose -f "$SCRIPT_DIR/docker-compose.infra.yml" down "$@"

# Pass -v to also remove volumes (wipes all data):
#   ./stop-infra.sh -v
