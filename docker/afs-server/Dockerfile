FROM myserver
MAINTAINER Rui Botelho "rui.botelho@tecnico.ulisboa.pt

RUN mkdir /code
COPY kerberos-init.sh /code
COPY afs-init.sh /code
COPY start.sh /code
RUN chmod a+x /code/*
