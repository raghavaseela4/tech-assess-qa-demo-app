#!/usr/bin/env bash
set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "==> Building claims-service..."
mvn package -Dmaven.test.skip=true -Dcheckstyle.skip -Dpmd.skip \
    -f "$SCRIPT_DIR/code/claims-service/pom.xml"

echo "==> Building bff-service..."
mvn package -Dmaven.test.skip=true -Dcheckstyle.skip -Dpmd.skip \
    -f "$SCRIPT_DIR/code/bff-service/pom.xml"

echo "==> Starting application services..."
docker compose -f "$SCRIPT_DIR/docker-compose.apps.yml" up --build --pull never "$@"
