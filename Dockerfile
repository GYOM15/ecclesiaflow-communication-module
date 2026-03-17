# ===========================================================================
# ECCLESIAFLOW EMAIL MODULE — Multi-stage Docker Build
# ===========================================================================
# Stage 1: Build with Maven
# Stage 2: Runtime with JRE only
# ===========================================================================

# ---------------------------------------------------------------------------
# Stage 1 — BUILD
# ---------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Cache dependencies first
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---------------------------------------------------------------------------
# Stage 2 — RUNTIME
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime

# Security: non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy the built JAR
COPY --from=build /app/target/*.jar app.jar

# Own files as appuser
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8082 9092

HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=60s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
