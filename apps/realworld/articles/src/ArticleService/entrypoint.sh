#!/bin/sh
set -eu
i=0
until npx prisma migrate deploy; do
  i=$((i+1))
  if [ "$i" -ge 30 ]; then
    echo "prisma migrate deploy failed"
    exit 1
  fi
  sleep 2
done
node dist/prisma/seed.js
exec gg ./package.json
