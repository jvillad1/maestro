FROM gradle:8.10.2-jdk17 AS build
WORKDIR /app

# Copy only what the server build needs
COPY gradle gradle
COPY gradlew gradlew
COPY settings-server.gradle.kts settings.gradle.kts
COPY gradle.properties gradle.properties
COPY build.gradle.kts build.gradle.kts
COPY core core
COPY server server

# Build server fat JAR (same pattern as movi: java -jar app.jar)
RUN gradle :server:shadowJar --no-daemon -x test

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/server/build/libs/server.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
