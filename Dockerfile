# ==============================================================================
# Multi-Stage Dockerfile for Digital Wallet API (Java 21 LTS / Spring Boot 3)
# ==============================================================================

# Stage 1: Build & Package
FROM maven:3.9.8-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Cache Maven dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy application source code
COPY src ./src

# Compile and package application (skipping tests since they run in CI)
RUN mvn clean package -DskipTests

# Stage 2: Production-grade Minimal JRE Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Security: Create non-root system group and user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy executable jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Set ownership to unprivileged user
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

# Production JVM optimizations for containerized workloads
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
