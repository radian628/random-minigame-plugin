#!/bin/bash
cd "$(dirname "$0")"
mvn install
./send-jar.sh
# mv -f target/random-minigame-1.0-SNAPSHOT.jar ../../plugins
