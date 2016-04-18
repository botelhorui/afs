#!/bin/bash

source `dirname $0`/configureKerberosClient.sh
source `dirname $0`/configureAFSClient.sh

echo "================================================================"
echo "==== Tests ====================================================="
echo "================================================================"

sbt test
