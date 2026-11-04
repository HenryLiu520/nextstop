# Build with Maven, run on a JRE. Tests are not run here: CI runs them, and an image build
# should not fail because of a flaky test.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src

# Dependencies first, so a code change does not re-download the world
COPY pom.xml ./
RUN mvn -B -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as a normal user, and own the upload directory so photos can be written
RUN useradd --create-home --uid 10001 app \
 && mkdir -p /app/uploads/review-photos \
 && chown -R app:app /app

COPY --from=build --chown=app:app /src/target/*.jar /app/app.jar

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
