# Same shape as the main backend: build with Maven, run on a JRE.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src

COPY pom.xml ./
RUN mvn -B -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --create-home --uid 10002 app && chown -R app:app /app
COPY --from=build --chown=app:app /src/target/*.jar /app/app.jar
USER app
EXPOSE 9000
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
