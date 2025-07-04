# Build stage
FROM gradle:8.5-jdk17-alpine AS build
WORKDIR /app

# Copy gradle files
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle ./gradle

# Copy common module
COPY common ./common

# Copy service source
COPY order-service ./order-service

# Build the service
RUN ./gradlew :order-service:build -x test

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Add user for security
RUN addgroup -g 1000 spring && \
    adduser -u 1000 -G spring -s /bin/sh -D spring

# Install utilities
RUN apk add --no-cache curl

WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/order-service/build/libs/*.jar app.jar

# Change ownership
RUN chown -R spring:spring /app

USER spring:spring

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8083/api/orders/hello || exit 1

# Expose ports
EXPOSE 8083 9094

# JVM options for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
