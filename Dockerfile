FROM openjdk:8-jre
RUN wget http://get.docker.com/builds/Linux/x86_64/docker-latest.tgz
RUN tar -xvzf docker-latest.tgz
RUN mv docker/* /usr/bin
COPY server/target/universal/docker-stats-monitor /docker-stats-monitor
EXPOSE 8080
WORKDIR /docker-stats-monitor
ENTRYPOINT [ "./bin/server" ]