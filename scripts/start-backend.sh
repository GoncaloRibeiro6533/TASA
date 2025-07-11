#! /bin/bash


NGINX_EXTERNAL_PORT="${1:-8080}"

export NGINX_EXTERNAL_PORT

set -e

cd ..
cd jvm

./gradlew startAll

# Wait for the services to start
while ! curl -s "http://localhost:${NGINX_EXTERNAL_PORT}" > /dev/null; do
    sleep 1
done

echo "All services started successfully."
