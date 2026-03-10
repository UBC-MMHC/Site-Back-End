# Build context = service directory (Root Directory: services/newsletter-service)
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY pom.xml .
RUN mvn -B -f pom.xml dependency:go-offline -DskipTests || true

COPY src src
RUN mvn -B -f pom.xml package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /build/target/newsletter-service-*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]
