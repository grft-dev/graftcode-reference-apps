#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

docker compose up --build -d customers vets

customers_guid=""
for _ in $(seq 1 60); do
    if body=$(curl -fsS --max-time 5 "http://localhost:8092/maven" 2>/dev/null); then
        customers_guid=$(printf '%s' "$body" | grep -oE 'https://grft\.dev/maven2/[0-9a-f-]+__free' | head -n 1 | sed -E 's#https://grft\.dev/maven2/([0-9a-f-]+)__free#\1#' || true)
        if [ -n "$customers_guid" ]; then
            break
        fi
    fi
    sleep 2
done

if [ -z "$customers_guid" ]; then
    echo "Graftcode Gateway did not publish a Maven repository at http://localhost:8092/maven" >&2
    exit 1
fi

echo "customers.graft.guid=$customers_guid"

export GRAFT_GUID="$customers_guid"
docker compose build visits
docker compose up -d visits

visits_guid=""
for _ in $(seq 1 60); do
    if body=$(curl -fsS --max-time 5 "http://localhost:8096/npm" 2>/dev/null); then
        visits_guid=$(printf '%s' "$body" | grep -oE 'https://grft\.dev/[0-9a-f-]+__free' | head -n 1 | sed -E 's#https://grft\.dev/([0-9a-f-]+)__free#\1#' || true)
        if [ -n "$visits_guid" ]; then
            break
        fi
    fi
    sleep 2
done

if [ -z "$visits_guid" ]; then
    echo "Graftcode Gateway did not publish an npm registry at http://localhost:8096/npm" >&2
    exit 1
fi

echo "visits.graft.guid=$visits_guid"
printf '@graft:registry=https://grft.dev/%s__free\n' "$visits_guid" > ClinicWeb/.npmrc

(
    cd ClinicWeb
    npm install
    npm test
)

(
    cd visits
    mvn -q test "-Dgraft.guid=$customers_guid"
)
