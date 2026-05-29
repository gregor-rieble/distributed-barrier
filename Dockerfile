FROM maven:3.9-eclipse-temurin-25 AS build
ARG RELEASE_VERSION=1.0-SNAPSHOT

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn -B --no-transfer-progress versions:set -DnewVersion="$RELEASE_VERSION" && \
    mvn clean package -DskipTests


FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/target/distributed-barrier-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]