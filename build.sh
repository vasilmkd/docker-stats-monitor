#!/bin/bash
sbt clean fastOptJS server/universal:packageBin
cd server/target/universal
unzip server-*.zip
rm server-*.zip
mv server-* docker-stats-monitor
cd ../../..
docker build -t vasilvasilev97/docker-stats-monitor .
