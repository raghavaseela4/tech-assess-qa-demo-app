#!/bin/bash
set -e

# Create PostgreSQL publication for Debezium CDC (if claims table exists)
# Publication name must match connector config: dbz_publication

# Check if claims table exists before creating publication
TABLE_EXISTS=$(psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -tAc \
    "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'claims');")

if [ "$TABLE_EXISTS" = "t" ]; then
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
        -- Create publication for claims table
        -- This allows Debezium to subscribe to changes on the claims table
        CREATE PUBLICATION dbz_publication FOR TABLE public.claims;

        -- Grant replication permissions to debezium user
        GRANT SELECT ON public.claims TO debezium;
EOSQL
    echo "Debezium publication 'dbz_publication' created successfully for public.claims table"
else
    echo "Skipping publication creation - claims table doesn't exist yet"
    echo "Run the application first to create tables, then restart infrastructure"
fi
