#!/bin/bash
# Maven Wrapper Script

MAVEN_VERSION="3.9.7"
MAVEN_URL="https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz"
MAVEN_DIR="/usr/local"

if [ ! -d "$MAVEN_DIR" ]; then
    echo "Downloading Maven ${MAVEN_VERSION}..."
    mkdir -p "$MAVEN_DIR"
    curl -L "$MAVEN_URL" | tar -xz -C "$MAVEN_DIR" --strip-components=1
fi

exec "$MAVEN_DIR/bin/mvn" "$@"
