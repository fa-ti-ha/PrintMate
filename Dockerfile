# Multi-stage production build for PrintMate

# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace
ENV MAVEN_OPTS="-Xms32m -Xmx256m"

# Copy maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code and build package
COPY src src
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Install curl for healthcheck
RUN apk add --no-cache curl tzdata

# Set timezone
ENV TZ=Asia/Dhaka
RUN cp /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Create non-root system user and group
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Create uploads directory and permissions
RUN mkdir -p /app/uploads && chown -R appuser:appgroup /app

# Copy JAR from builder stage
COPY --from=builder --chown=appuser:appgroup /workspace/target/printmate-*.jar /app/app.jar

# Switch to non-root user
USER appuser

EXPOSE 8080 8081

# Healthcheck targeting Actuator management port
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
