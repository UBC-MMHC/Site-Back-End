# Build context = service directory (Root Directory: gateway)
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

COPY pom.xml .
RUN mvn -B -f pom.xml dependency:go-offline -DskipTests || true

COPY src src
RUN mvn -B -f pom.xml package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /build/target/gateway-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
