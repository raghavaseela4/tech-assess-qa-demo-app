#!/bin/bash
set -e

# Debezium CDC replication user setup
# Password read from DEBEZIUM_PASSWORD environment variable

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Create debezium replication user with password from env var
    CREATE USER debezium WITH REPLICATION LOGIN PASSWORD '$DEBEZIUM_PASSWORD';

    -- Grant minimal required privileges
    GRANT USAGE ON SCHEMA public TO debezium;
    GRANT SELECT ON ALL TABLES IN SCHEMA public TO debezium;
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO debezium;
EOSQL

echo "Debezium user created successfully with password from DEBEZIUM_PASSWORD env var"
