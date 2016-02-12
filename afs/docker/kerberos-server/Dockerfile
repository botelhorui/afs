FROM debian:stable
MAINTAINER Rui Botelho "rui.botelho@tecnico.ulisboa.pt

RUN apt-get -y update
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y krb5-kdc krb5-admin-server

COPY kerberos-init.sh /usr/local/bin/kerberos-init.sh
COPY start.sh /etc/my_init.d/00_kerberos_init.sh
RUN chmod a+x /usr/local/bin/kerberos-init.sh /etc/my_init.d/00_kerberos_init.sh

CMD /etc/my_init.d/00_kerberos_init.sh
