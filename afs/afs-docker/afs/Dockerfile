FROM debian:jessie
MAINTAINER Simão Martins "simao.martins@tecnico.ulisboa.pt"

#ENV TERM=linux
RUN apt-get -qq -y update && \
  DEBIAN_FRONTEND=noninteractive apt-get install -y vim krb5-user \
    openafs-client openafs-modules-dkms openafs-krb5 openafs-fileserver openafs-dbserver && \
  apt-get clean

CMD /tmp/afs/init-script.sh
