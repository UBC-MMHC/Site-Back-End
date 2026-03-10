# Stage 1: Build the JAR (build context = repo root on Railway)
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY services/newsletter-service/pom.xml services/newsletter-service/
RUN mvn -B -f services/newsletter-service/pom.xml dependency:go-offline -DskipTests || true

COPY services/newsletter-service services/newsletter-service
RUN mvn -B -f services/newsletter-service/pom.xml package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /build/services/newsletter-service/target/newsletter-service-*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]
