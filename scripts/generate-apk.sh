#! /bin/bash


NGINX_EXTERNAL_PORT="${1:-8080}"
# Start the frontend

#ask for URL to give to the frontend
read -p "Enter the URL for the frontend (default: http://localhost:${NGINX_EXTERNAL_PORT}): " FRONTEND_URL

FRONTEND_URL="${FRONTEND_URL:-http://localhost:${NGINX_EXTERNAL_PORT}}"

cd ..

cd frontend/app/src/main/assets || exit

sed -i "s|^api_url=.*|api_url=${FRONTEND_URL}|g" config.properties

# create apk

cd ..
cd ..
cd ..
cd ..

./gradlew assembleDebug


echo "APK created successfully. You can find it in the app/build/outputs/apk/debug directory."
