FROM debian:stable
MAINTAINER Rui Botelho "rui.botelho@tecnico.ulisboa.pt
RUN apt-get -y update
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y krb5-user
COPY init.sh /code/init.sh
RUN chmod a+x /code/init.sh
CMD /code/init.sh
