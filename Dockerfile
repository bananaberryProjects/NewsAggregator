# Multi-Stage Dockerfile für News Aggregator
# Stage 1: Build Backend
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Kopiere Maven Wrapper und pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Mache mvnw ausführbar
RUN chmod +x mvnw

# Kopiere Source Code
COPY src src

# Baue die Anwendung (skip Tests für schnelleren Build)
RUN ./mvnw clean package -DskipTests

# Stage 2: Build Frontend (Node.js)
FROM node:20-alpine AS frontend-builder

WORKDIR /app

# Kopiere Frontend-Dateien
COPY frontend/package*.json .
RUN npm ci

COPY frontend .
RUN npm run build

# Stage 3: Runtime Image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Installiere PostgreSQL Client (optional, für Healthchecks)
RUN apk add --no-cache postgresql-client

# Erstelle Verzeichnis für Frontend-Assets
RUN mkdir -p /app/static

# Kopiere das gebaute JAR vom Builder
COPY --from=builder /app/target/*.jar app.jar

# Kopiere das Frontend-Build
COPY --from=frontend-builder /app/dist /app/static

# Exponiere Ports
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Starte die Anwendung
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "app.jar"]
