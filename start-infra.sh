#!/usr/bin/env bash
set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "==> Starting infrastructure..."
docker compose -f "$SCRIPT_DIR/docker-compose.infra.yml" up -d "$@"

echo ""
echo "Infrastructure running:"
echo "  Keycloak:   http://localhost:8180  (admin / Admin123!)"
echo "  Kafka UI:   http://localhost:9093"
echo "  PostgreSQL: localhost:5432"
echo "  Redis:      localhost:6379"
