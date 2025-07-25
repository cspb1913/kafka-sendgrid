# Build stage
FROM gradle:8.11.1-jdk17-alpine AS build

# Set working directory
WORKDIR /app

# Copy gradle files
COPY build.gradle .
COPY gradle ./gradle
COPY gradlew .
COPY settings.gradle* ./

# Copy source code
COPY src ./src

# Build the application
RUN ./gradlew build --no-daemon -x test

# Runtime stage - using Java 17 Alpine 3.21
FROM eclipse-temurin:17-jre-alpine

# Install dumb-init for proper signal handling
RUN apk add --no-cache dumb-init

# Create application user
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/build/libs/kafka-sendgrid-*.jar app.jar

# Change ownership of the app directory
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose the port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Use dumb-init to properly handle signals
ENTRYPOINT ["dumb-init", "--"]

# Run the application
CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]