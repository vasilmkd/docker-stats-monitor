#!/bin/bash
sbt clean compile fastOptJS server/universal:packageBin
cd server/target/universal
unzip server-*.zip
cd ../../..
docker build -t vasilvasilev97/docker-stats-monitor .
