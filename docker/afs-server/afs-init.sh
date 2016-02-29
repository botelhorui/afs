#!/usr/bin/env bash

CELLNAME=example.com
SERVERNAME=$HOSTNAME

# starting bosserver
/usr/sbin/bosserver -noauth &
/usr/sbin/bosserver -noauth
/usr/bin/bos setcellname $SERVERNAME $CELLNAME -noauth
bos listhosts $SERVERNAME -noauth

#creating database server instances
bos create $SERVERNAME ptserver simple /usr/lib/openafs/ptserver -cell $CELLNAME -noauth
bos create $SERVERNAME vlserver simple /usr/lib/openafs/vlserver -cell $CELLNAME -noauth

# Adding privileged users
bos adduser $SERVERNAME admin -cell $CELLNAME -noauth
bos adduser $SERVERNAME avinesh -cell $CELLNAME -noauth
bos listusers -s $SERVERNAME -cell $CELLNAME -noauth

# Adding users to AFS protection server's database
pts createuser -name admin   -noauth
pts createuser -name avinesh   -noauth
pts adduser admin system:administrators -noauth
pts adduser avinesh system:administrators -noauth
pts membership admin -noauth
pts membership avinesh -noauth
pts listentries -c $CELLNAME -localauth