#!/usr/bin/env bash

kadmin.local -q "addprinc -policy service -randkey -e des-cbc-crc:normal afs/example.com"
kadmin.local -q "ktadd -e des-cbc-crc:normal -k /etc/krb5.keytab.afs afs/example.com"

klist -kte /etc/krb5.keytab.afs

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

# add keys to be used by AFS
asetkey add 2 /etc/krb5.keytab.afs afs/example.com

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