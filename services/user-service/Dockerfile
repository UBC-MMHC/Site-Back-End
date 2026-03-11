# Build context = service directory (Root Directory: services/user-service)
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

COPY pom.xml .
RUN mvn -B -f pom.xml dependency:go-offline -DskipTests || true

COPY src src
RUN mvn -B -f pom.xml package -DskipTests

# Stage 2: Runtime image (non-Alpine for Railway private networking DNS compatibility)
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /build/target/user-service-*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
