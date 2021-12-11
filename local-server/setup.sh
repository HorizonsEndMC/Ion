#!/bin/bash

cp template data -r
./download_plugins.sh
./update.sh
docker-compose build
