# Stage 1: Build the JAR (build context = repo root on Railway)
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY gateway/pom.xml gateway/
RUN mvn -B -f gateway/pom.xml dependency:go-offline -DskipTests || true

COPY gateway gateway
RUN mvn -B -f gateway/pom.xml package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /build/gateway/target/gateway-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
