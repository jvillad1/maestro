#!/bin/bash
set -e
sed -i 's/include(":shared", ":server", ":composeApp")/include(":shared", ":server")/' settings.gradle.kts
./gradlew :server:installDist -x test --no-daemon
