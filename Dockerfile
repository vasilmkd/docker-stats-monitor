FROM oracle/graalvm-ce:20.2.0-java11 as builder
RUN gu install native-image
RUN curl https://bintray.com/sbt/rpm/rpm | tee /etc/yum.repos.d/bintray-sbt-rpm.repo && \
    yum install -y sbt
COPY . /build
WORKDIR /build
RUN curl -L -o musl.tar.gz \
    https://github.com/gradinac/musl-bundle-example/releases/download/v1.0/musl.tar.gz && \
    tar -xvzf musl.tar.gz
RUN sbt clean compile fullOptJS
RUN sbt server/graalvm-native-image:packageBin
RUN rm static/js/client.js.map
RUN curl -L -o docker-latest.tgz http://get.docker.com/builds/Linux/x86_64/docker-latest.tgz
RUN tar -xvzf docker-latest.tgz

FROM scratch
COPY --from=builder /build/server/target/graalvm-native-image/server /server
COPY --from=builder /build/static /static
COPY --from=builder /build/docker/docker /docker
ENV PATH "/"
EXPOSE 8080
ENTRYPOINT [ "/server" ]
