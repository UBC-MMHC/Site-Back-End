# Build context = repository root
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

COPY pom.xml .
COPY app/pom.xml app/pom.xml
RUN mvn -B -pl app -am dependency:go-offline -DskipTests || true

COPY app app
RUN mvn -B -pl app -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /build/app/target/app-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
