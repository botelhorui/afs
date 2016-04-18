#!/bin/bash

# Remove any previously existing containers
docker rm -f `docker ps -qa | xargs`

# Build the containers
docker-compose build

# Run them
docker-compose up