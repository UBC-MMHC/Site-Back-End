# Stage 1: Build the JAR (build context = repo root on Railway)
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY services/user-service/pom.xml services/user-service/
RUN mvn -B -f services/user-service/pom.xml dependency:go-offline -DskipTests || true

COPY services/user-service services/user-service
RUN mvn -B -f services/user-service/pom.xml package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /build/services/user-service/target/user-service-*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
