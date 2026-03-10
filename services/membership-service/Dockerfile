# Stage 1: Build the JAR (build context = repo root on Railway)
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# Copy POM and source (membership-service has its own parent, no reactor needed)
COPY services/membership-service/pom.xml services/membership-service/
RUN mvn -B -f services/membership-service/pom.xml dependency:go-offline -DskipTests || true

COPY services/membership-service services/membership-service
RUN mvn -B -f services/membership-service/pom.xml package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /build/services/membership-service/target/membership-service-*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]
