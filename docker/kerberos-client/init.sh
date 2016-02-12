#!/usr/bin/env bash

REALM="EXAMPLE.COM"
DOMAIN="example.com"
CONTAINER_IP=kdc
ADMIN_PASSWORD="MITiys4K5"

# this is the noob way of waiting for the kdc to be setup
sleep 8 
echo -e "\n\$host kdc"
host kdc

echo -e "\n\$ping -c3 kdc"
ping -c3 kdc

echo -e "\n\$cat /etc/hosts"
cat /etc/hosts
 
echo -e  "\nConfiguring krb5-user on the local machine"
# We must configure kerberos on the local machine so we can use kadmin and kinit commands

cat > /etc/krb5.conf <<EOF
[libdefaults]
	default_realm = $REALM
	dns_lookup_realm = false
	dns_lookup_kdc = false
[realms]
	$REALM = {
		kdc = $CONTAINER_IP:750
		admin_server = $CONTAINER_IP:749
		default_domain = $DOMAIN
	}
[domain_realm]
	.$DOMAIN = $REALM
	$DOMAIN = $REALM
EOF

echo "
\$cat /etc/krb5.conf"
cat /etc/krb5.conf

echo "
Trying kinit kadmin/admin@$REALM (should work)"
kinit kadmin/admin@$REALM <<EOF
$ADMIN_PASSWORD
EOF

echo -e "\nKlist"
klist && echo -e "\nKerberos fully operational"

echo -e "\nKadmin"
kadmin -p kadmin/admin@$REALM <<EOF
$ADMIN_PASSWORD
get_principal kadmin/admin@$REALM
EOF

tail -f /dev/null
