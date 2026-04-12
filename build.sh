#!/bin/bash

# News Aggregator - Build Script
# Dieses Script baut die Anwendung und führt alle Tests aus

echo "=========================================="
echo "News Aggregator - Build & Test Script"
echo "=========================================="
echo ""

# Prüfe ob Maven installiert ist
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven ist nicht installiert!"
    echo ""
    echo "Installationsanleitung:"
    echo "  - Debian/Ubuntnu: sudo apt-get install maven"
    echo "  - macOS: brew install maven"
    echo "  - Oder besuchen Sie: https://maven.apache.org/install.html"
    exit 1
fi

# Prüfe Maven Version
echo "📋 Maven Version:"
mvn -version
echo ""

# Wechsle in das Projektverzeichnis
cd "$(dirname "$0")"

echo "=========================================="
echo "🔨 Schritt 1: Kompilieren"
echo "=========================================="
mvn clean compile
if [ $? -ne 0 ]; then
    echo "❌ Kompilieren fehlgeschlagen!"
    exit 1
fi
echo "✅ Kompilieren erfolgreich!"
echo ""

echo "=========================================="
echo "🧪 Schritt 2: Tests ausführen"
echo "=========================================="
mvn test
if [ $? -ne 0 ]; then
    echo "❌ Tests fehlgeschlagen!"
    exit 1
fi
echo "✅ Alle Tests bestanden!"
echo ""

echo "=========================================="
echo "📦 Schritt 3: Package erstellen"
echo "=========================================="
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ Package erstellen fehlgeschlagen!"
    exit 1
fi
echo "✅ Package erfolgreich erstellt!"
echo ""

echo "=========================================="
echo "🎉 Build erfolgreich!"
echo "=========================================="
echo ""
echo "Anwendung starten mit:"
echo "  mvn spring-boot:run"
echo ""
echo "Oder direkt mit JAR:"
echo "  java -jar target/news-aggregator-1.0.0.jar"
echo ""
echo "Die Anwendung ist dann erreichbar unter:"
echo "  http://localhost:8080/"
echo ""
