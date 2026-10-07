#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

docker compose up --build -d

guid=""
for _ in $(seq 1 30); do
    if body=$(curl -fsS --max-time 5 "http://localhost:8090/npm" 2>/dev/null); then
        guid=$(printf '%s' "$body" | grep -oE 'https://grft\.dev/[0-9a-f-]+__free' | head -n 1 | sed -E 's#https://grft\.dev/([0-9a-f-]+)__free#\1#' || true)
        if [ -n "$guid" ]; then
            break
        fi
    fi
    sleep 2
done

if [ -z "$guid" ]; then
    echo "Graftcode Gateway did not publish an npm registry at http://localhost:8090/npm" >&2
    exit 1
fi

echo "graft.guid=$guid"
printf '@graft:registry=https://grft.dev/%s__free\n' "$guid" > ClinicWeb/.npmrc

cd ClinicWeb
npm install
npm test
