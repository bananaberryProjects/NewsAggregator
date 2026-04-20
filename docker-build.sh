#!/bin/bash

# Docker Build Script für News Aggregator

set -e

echo "🚀 Building News Aggregator Docker Image..."

# Prüfe ob Docker installiert ist
if ! command -v docker &> /dev/null; then
    echo "❌ Docker ist nicht installiert!"
    exit 1
fi

# Baue das Image
docker build -t news-aggregator:latest .

echo "✅ Docker Image erfolgreich gebaut: news-aggregator:latest"
echo ""
echo "Um die Anwendung zu starten:"
echo "  docker-compose up -d"
echo ""
echo "Um die Anwendung zu stoppen:"
echo "  docker-compose down"
echo ""
echo "Logs anzeigen:"
echo "  docker logs -f news-aggregator-app"
