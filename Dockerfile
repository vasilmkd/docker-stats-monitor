FROM ghcr.io/graalvm/graalvm-ce:java11-21.1.0 as builder

# Install native-image
RUN gu install native-image

# Install sbt
RUN curl https://bintray.com/sbt/rpm/rpm | tee /etc/yum.repos.d/bintray-sbt-rpm.repo && \
    microdnf install -y sbt

# Static image requirements
RUN mkdir /staticlibs && \
    curl -L -o musl.tar.gz https://musl.libc.org/releases/musl-1.2.1.tar.gz && \
    mkdir musl && tar -xvzf musl.tar.gz -C musl --strip-components 1 && cd musl && \
    ./configure --disable-shared --prefix=/staticlibs && \
    make && make install && \
    cd / && rm -rf /muscl && rm -f /musl.tar.gz && \
    cp /usr/lib/gcc/x86_64-redhat-linux/8/libstdc++.a /staticlibs/lib/

ENV PATH="$PATH:/staticlibs/bin"
ENV CC="musl-gcc"

RUN curl -L -o zlib.tar.gz https://zlib.net/zlib-1.2.11.tar.gz && \
    mkdir zlib && tar -xvzf zlib.tar.gz -C zlib --strip-components 1 && cd zlib && \
    ./configure --static --prefix=/staticlibs && \
    make && make install && \
    cd / && rm -rf /zlib && rm -f /zlib.tar.gz

# Copy the build files
COPY . /build
WORKDIR /build

# Build the native image
RUN sbt clean compile fullOptJS
RUN sbt server/graalvm-native-image:packageBin
RUN rm static/js/client.js.map
RUN curl -L -o docker-latest.tgz http://get.docker.com/builds/Linux/x86_64/docker-latest.tgz
RUN tar -xvzf docker-latest.tgz

# Copy the native image to an empty container
FROM scratch
COPY --from=builder /build/server/target/graalvm-native-image/server /server
COPY --from=builder /build/static /static
COPY --from=builder /build/docker/docker /docker
ENV PATH "/"
EXPOSE 8080
ENTRYPOINT [ "/server" ]
