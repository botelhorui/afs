#!/bin/bash
# Be sure you already have kerberos configured. Otherwise this will fail.
echo "================================================================"
echo "==== AFS Client ================================================"
echo "================================================================"
AFS_SERVER_IP=$(host $AFS_SERVER | grep -oP "(?<=address )[^\n]+" | head -n1)
echo "AFS_SERVER_IP: $AFS_SERVER_IP"
echo "AFS_SERVER: $AFS_SERVER"

echo -e "$AFS_SERVER_IP\t$AFS_SERVER.$CELL_NAME" | tee -a /etc/hosts

function setThisCellAndCellServDB {
  # This overwrites the files
  echo $CELL_NAME > ThisCell
  echo -e ">$CELL_NAME\n$AFS_SERVER_IP\t\t\t#$AFS_SERVER.$CELL_NAME" > CellServDB
}

cd /etc/openafs
setThisCellAndCellServDB

# The most important flag being set here is AFS_DYNROOT.
# If this is set to true a myriad of problems will spawn further ahead.
# It is also important that AFSDB is set to true
cat <<EOF > afs.conf.client
AFS_CLIENT=true
AFS_AFSDB=false
AFS_CRYPT=false
AFS_DYNROOT=false
AFS_FAKESTAT=true
EOF

# Give some time for afs server to initialize
modprobe openafs
# It seems that the afs kernel module cannot be used simultaneously by two (or more) containers.
# So we need to wait for the AFS server to relinquish its hold on it.
while [[ $(lsmod | grep openafs) ]]; do
  >&2 echo "Module still in use - sleeping 30 secs"
  sleep 5
done
echo ""

/etc/init.d/openafs-client start
fs checkservers

kinit $AFSADMIN_PRINCIPAL@$REALM <<< $AFSADMIN_PASSWORD
aklog -d
echo ""

fs listacl /afs/$CELL_NAME
echo ""

ls -l /afs/$CELL_NAME