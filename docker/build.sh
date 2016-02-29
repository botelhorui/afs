#!/usr/bin/env bash

# 1. myserver contains all needed packages
docker build -t myserver myserver/
# 2 afs-server...
docker build -t afs-server afs-server/

