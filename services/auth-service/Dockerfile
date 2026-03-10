# Stage 1: Build the JAR (build context = repo root on Railway)
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY services/auth-service/pom.xml services/auth-service/
RUN mvn -B -f services/auth-service/pom.xml dependency:go-offline -DskipTests || true

COPY services/auth-service services/auth-service
RUN mvn -B -f services/auth-service/pom.xml package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /build/services/auth-service/target/auth-service-*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
