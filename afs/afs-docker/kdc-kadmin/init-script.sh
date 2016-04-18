#!/usr/bin/env bash

echo "================================================================"
echo "==== Kerberos KDC and Kadmin ==================================="
echo "================================================================"
[[ -z $DOMAIN ]] && DOMAIN=$(hostname -d)
# If the domain is still empty then the user did not define a hostname
# In these cases we use the example.com
[[ -z $DOMAIN ]] && DOMAIN=example.com
[[ -z $REALM ]] && REALM=$(echo $DOMAIN | sed 's/.*/\U&/')
[[ -z $SUPPORTED_ENCRYPTION_TYPES ]] && SUPPORTED_ENCRYPTION_TYPES=aes256-cts-hmac-sha1-96:normal
[[ -z $KDC_KADMIN_SERVER ]] && KDC_KADMIN_SERVER=$(hostname -f)
[[ -z $KADMIN_PRINCIPAL ]] && KADMIN_PRINCIPAL=kadmin/admin
[[ -z $KADMIN_PASSWORD ]] && KADMIN_PASSWORD=MITiys4K5
KADMIN_PRINCIPAL_FULL=$KADMIN_PRINCIPAL@$REALM

echo "DOMAIN: $DOMAIN"
echo "REALM: $REALM"
echo "KDC_KADMIN_SERVER: $KDC_KADMIN_SERVER"
echo "KADMIN_PRINCIPAL_FULL: $KADMIN_PRINCIPAL_FULL"
echo "KADMIN_PASSWORD: $KADMIN_PASSWORD"
echo ""

echo "================================================================"
echo "==== /etc/krb5.conf ============================================"
echo "================================================================"
tee /etc/krb5.conf <<EOF
[libdefaults]
	default_realm = $REALM
	dns_lookup_realm = false
	dns_lookup_kdc = false

[realms]
	$REALM = {
		kdc = $KDC_KADMIN_SERVER:88
		admin_server = $KDC_KADMIN_SERVER:749
		default_domain = $DOMAIN
	}

[domain_realm]
	.$DOMAIN = $REALM
	$DOMAIN = $REALM
EOF
echo ""

echo "================================================================"
echo "==== /etc/krb5kdc/kdc.conf ====================================="
echo "================================================================"
tee /etc/krb5kdc/kdc.conf <<EOF
[realms]
	$REALM = {
		acl_file = /etc/krb5kdc/kadm5.acl
		max_renewable_life = 7d 0h 0m 0s
		supported_enctypes = $SUPPORTED_ENCRYPTION_TYPES
		default_principal_flags = +preauth
	}

[logging]
	default = FILE:/tmp/krb5libs.log
	kdc = FILE:/tmp/kdc.log
	admin_server = FILE:/tmp/kadmin.log
EOF
echo ""

echo "================================================================"
echo "==== /etc/krb5kdc/kadm5.acl ===================================="
echo "================================================================"
tee /etc/krb5kdc/kadm5.acl <<EOF
$KADMIN_PRINCIPAL_FULL *
noPermissions@$REALM X
EOF
echo ""

echo "================================================================"
echo "==== Creating realm ============================================"
echo "================================================================"
MASTER_PASSWORD=$(tr -cd '[:alnum:]' < /dev/urandom | fold -w30 | head -n1)
# This command also starts the krb5-kdc and krb5-admin-server services
krb5_newrealm <<EOF
$MASTER_PASSWORD
$MASTER_PASSWORD
EOF
echo ""

echo "================================================================"
echo "==== Create the principals in the acl =========================="
echo "================================================================"
echo "Adding $KADMIN_PRINCIPAL principal"
kadmin.local -q "delete_principal -force $KADMIN_PRINCIPAL_FULL"
kadmin.local -q "addprinc -pw $KADMIN_PASSWORD $KADMIN_PRINCIPAL_FULL"
echo ""

echo "Adding noPermissions principal"
kadmin.local -q "delete_principal -force noPermissions@$REALM"
kadmin.local -q "addprinc -pw $KADMIN_PASSWORD noPermissions@$REALM"
echo ""

echo "================================================================"
echo "==== Run the services =========================================="
echo "================================================================"
# We want the container to keep running until we explicitly kill it.
# So the last command cannot immediately exit. See
#   https://docs.docker.com/engine/reference/run/#detached-vs-foreground
# for a better explanation.

krb5kdc
kadmind -nofork