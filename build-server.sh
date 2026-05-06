#!/bin/bash
set -e
./gradlew :server:shadowJar -x test --no-daemon --settings-file settings-server.gradle.kts
