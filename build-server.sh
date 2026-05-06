#!/bin/bash
set -e
./gradlew :server:installDist -x test --no-daemon --settings-file settings-server.gradle.kts
