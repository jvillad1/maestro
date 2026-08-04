FROM gradle:8.10.2-jdk17 AS build
WORKDIR /app
COPY . .

# Build wasmJs production bundle
RUN gradle :webApp:wasmJsBrowserDistribution --no-daemon --quiet

# Copy web app into server resources so it's bundled in the fat JAR
RUN mkdir -p server/src/main/resources/static && \
    cp -r webApp/build/dist/wasmJs/productionExecutable/. server/src/main/resources/static/

# Build server fat JAR (now includes the web app)
RUN gradle :server:shadowJar --no-daemon --quiet -x test

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/server/build/libs/server.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
