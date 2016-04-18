#!/bin/bash
source `dirname $0`/configureKerberosClient.sh

echo "================================================================"
echo "==== AFS ======================================================="
echo "================================================================"
CELL_PRINCIPAL=afs/$CELL_NAME@$REALM
AFSADMIN_PRINCIPAL_FULL=$AFSADMIN_PRINCIPAL@$REALM
AFSADMIN_USER=${AFSADMIN_PRINCIPAL/\//.} # Replace / with .
echo "CELL_NAME: $CELL_NAME"
echo "CELL_PRINCIPAL: $CELL_PRINCIPAL"
echo "AFSADMIN_PRINCIPAL_FULL: $AFSADMIN_PRINCIPAL_FULL"
echo "AFSADMIN_PASSWORD: $AFSADMIN_PASSWORD"
echo "AFSADMIN_USER: $AFSADMIN_USER"
echo "ENCRYPTION_TYPES: $ENCRYPTION_TYPES"
echo ""

echo "================================================================================"
echo "==== Create $CELL_PRINCIPAL Principal =============================="
echo "================================================================================"
kadminCommand "addprinc -randkey $CELL_PRINCIPAL"
kadminCommand "ktadd -k /etc/openafs/server/rxkad.keytab -e $ENCRYPTION_TYPES $CELL_PRINCIPAL"
echo ""

echo "================================================================================"
echo "==== Configure ThisCell, CellServDB and Start File server ======================"
echo "================================================================================"
AFS_SERVER_IP=$(cat /etc/hosts | grep $HOSTNAME | cut -f1 | head -n1)

function setThisCellAndCellServDB {
  echo $CELL_NAME > ThisCell
  echo -e ">$CELL_NAME\n$AFS_SERVER_IP\t\t\t#$HOSTNAME" > CellServDB
}

# Client
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

# Server
cd /etc/openafs/server
setThisCellAndCellServDB

/etc/init.d/openafs-fileserver start
# Confirm the server CellServDB is correct
bos listhosts $HOSTNAME -localauth
echo ""

echo "================================================================================"
echo "==== Create Protection, Volume Location and Demand-Attach File servers ========="
echo "================================================================================"
# The /vicepa partition must be created BEFORE the file server is started
mkdir -p /vicepa
touch /vicepa/AlwaysAttach

function createServer() {
    bos create $HOSTNAME $1 simple /usr/lib/openafs/$1 -localauth && echo "bos: instance $1 created"
}

createServer ptserver
createServer vlserver
createServer buserver
bos create $HOSTNAME dafs dafs \
           /usr/lib/openafs/dafileserver   \
           /usr/lib/openafs/davolserver \
           /usr/lib/openafs/salvageserver \
           /usr/lib/openafs/dasalvager \
           -localauth && echo "bos: created instance 'dafs'"
echo ""

# Confirm that every server has started normally
bos status $HOSTNAME -localauth
echo ""

echo "================================================================================"
echo "==== Create the volumes root.afs and root.cell ================================="
echo "================================================================================"
vos create $HOSTNAME /vicepa root.afs -localauth
vos create $HOSTNAME /vicepa root.cell -localauth
echo ""

vos listvol $HOSTNAME -localauth

echo "================================================================================"
echo "==== Create $AFSADMIN_PRINCIPAL_FULL Principal and add it to BOS and PTS ====="
echo "================================================================================"
kadminCommand "addprinc -pw $AFSADMIN_PASSWORD -e $ENCRYPTION_TYPES $AFSADMIN_PRINCIPAL_FULL"
echo ""

echo "Adding $AFSADMIN_USER to BOS"
# If the next line is uncommented all bos commands that follow it will produce an output similar to:
#  bos: failed to delete user 'afsadmin.admin', (communications failure (-1))
# It seems that performing a remove without ever having performed an add causes it to enter a failed state.
#bos removeuser $HOSTNAME AFSADMIN_USER -localauth
bos adduser $HOSTNAME $AFSADMIN_USER -localauth # This creates /etc/openafs/server/UserList
bos listusers $HOSTNAME -localauth
echo ""

#sleep 1

echo "Adding $AFSADMIN_USER to the Protection Database"
pts createuser -name $AFSADMIN_USER -localauth
pts adduser $AFSADMIN_USER system:administrators -localauth
pts membership $AFSADMIN_USER -localauth
echo ""

echo "================================================================================"
echo "==== Configuring the Top Levels of the AFS Filespace ==========================="
echo "================================================================================"
/etc/init.d/openafs-client start
echo ""

kinit $AFSADMIN_PRINCIPAL_FULL <<< $AFSADMIN_PASSWORD
aklog -d
echo ""

EXPECTED_ID=$(pts examine $AFSADMIN_USER -localauth | grep -Po "(?<=id: )[^,]+")
OBTAINED_ID=$(tokens | grep -Po "(?<=AFS ID )\d+")
if [ $EXPECTED_ID != $OBTAINED_ID ]; then
    echo "The obtained AFS ID ($OBTAINED_ID) did not match the expected ID ($EXPECTED_ID)!"
    echo "fs commands will fail stating that you do not have the necessary permissions."
    echo ""
fi

# The fs mkmount for the /afs has already been made by the openafs-client star
fs setacl /afs system:anyuser rl
fs examine /afs
echo ""

fs mkmount /afs/$CELL_NAME root.cell -cell $CELL_NAME
fs setacl /afs/$CELL_NAME system:anyuser rl system:administrators all
fs examine /afs/$CELL_NAME
echo ""

fs mkmount /afs/.$CELL_NAME root.cell -cell $CELL_NAME -rw
fs setacl /afs/.$CELL_NAME system:anyuser rl system:administrators all
fs examine /afs/.$CELL_NAME
echo ""

vos addsite $HOSTNAME /vicepa root.afs -localauth
vos addsite $HOSTNAME /vicepa root.cell -localauth
echo ""

vos release root.afs
vos release root.cell
echo ""

/etc/init.d/openafs-client stop

/etc/init.d/openafs-fileserver stop
echo "Starting BOS server in foreground"
bosserver -nofork

