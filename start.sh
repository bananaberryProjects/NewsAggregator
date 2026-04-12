#!/bin/bash
# News Aggregator Start Script

echo "Starting News Aggregator..."

# Option 1: Mit Docker Compose
if command -v docker-compose &> /dev/null; then
    echo "Starting with Docker Compose..."
    docker-compose up --build -d
    echo "App läuft auf http://localhost:8080"
    exit 0
fi

# Option 2: Mit Maven (wenn installiert)
if command -v mvn &> /dev/null; then
    echo "Starting with Maven..."
    mvn spring-boot:run
    exit 0
fi

# Option 3: Fehlermeldung
echo "Fehler: Weder Docker Compose noch Maven gefunden!"
echo "Bitte installiere eines der beiden:"
echo "  - Docker Compose: https://docs.docker.com/compose/install/"
echo "  - Maven: https://maven.apache.org/install.html"
exit 1
