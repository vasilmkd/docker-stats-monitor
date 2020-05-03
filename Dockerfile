FROM openjdk:8-jre
RUN wget http://get.docker.com/builds/Linux/x86_64/docker-latest.tgz
RUN tar -xvzf docker-latest.tgz
RUN mv docker/* /usr/bin
COPY server/target/universal/docker-stats-monitor /docker-stats-monitor
EXPOSE 9000
ENTRYPOINT [ "/docker-stats-monitor/bin/server", "-Dplay.http.secret.key='dockerstatsmonitor'" ]