FROM gradle:8.10.2-jdk17 AS build
WORKDIR /app

# Copy only what the server build needs
COPY gradle gradle
COPY gradlew gradlew
COPY settings.gradle.kts settings.gradle.kts
COPY gradle.properties gradle.properties
COPY build.gradle.kts build.gradle.kts
COPY shared shared
COPY server server

# Remove composeApp from settings so Gradle doesn't look for a missing directory
RUN sed -i 's/include(":shared", ":server", ":composeApp")/include(":shared", ":server")/' settings.gradle.kts

# Build server distribution
RUN gradle :server:installDist --no-daemon -x test

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/server/build/install/server .
EXPOSE 8080
CMD ["bin/server"]
