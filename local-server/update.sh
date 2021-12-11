#!/bin/bash

cd "$(dirname "$0")" || exit

echo "Stopping..."
./stop.sh

cd ..

echo "Clearing..."
rm build/libs -r
#export GLOBIGNORE='build/libs/StarLegacyLibs.jar'
echo "Building..."
./gradlew build --parallel -x test || exit

echo "Copying..."
cp build/libs/*.jar ./local-server/data/plugins

cd ./local-server || exit

echo "Starting..."
./start.sh
