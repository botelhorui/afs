#!/bin/bash
echo "================================================================"
echo "==== Kerberos Client ==========================================="
echo "================================================================"
KADMIN_PRINCIPAL_FULL=$KADMIN_PRINCIPAL@$REALM
echo "DOMAIN: $DOMAIN"
echo "REALM: $REALM"
echo "KDC_KADMIN_SERVER: $KDC_KADMIN_SERVER"
echo "KADMIN_PRINCIPAL_FULL: $KADMIN_PRINCIPAL_FULL"
echo "KADMIN_PASSWORD: $KADMIN_PASSWORD"
echo ""

function kadminCommand {
    kadmin -p $KADMIN_PRINCIPAL_FULL -w $KADMIN_PASSWORD -q "$1"
}

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
	}

[domain_realm]
	.$DOMAIN = $REALM
	$DOMAIN = $REALM
EOF
echo ""

echo "================================================================"
echo "==== Testing ==================================================="
echo "================================================================"
echo "Testing Kadmin"
until kadminCommand "get_principal $KADMIN_PRINCIPAL_FULL"; do
  >&2 echo "KDC is unavailable - sleeping 1 sec"
  sleep 1
done
echo ""