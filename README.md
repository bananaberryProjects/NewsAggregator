# 📰 News Aggregator

Ein Spring Boot basiertes News-Aggregationssystem mit hexagonaler Architektur, PostgreSQL-Datenbank und **React Frontend**.

## 🏗️ Architektur

Das Projekt folgt der **Hexagonalen Architektur** (Clean Architecture / Ports and Adapters) mit einem modernen React Frontend:

```
news-aggregator/
├── backend/                    # Spring Boot API
│   ├── src/main/java/
│   │   ├── domain/            # Domain Layer - Reine Business-Logik
│   │   ├── application/       # Application Layer - Use Cases
│   │   └── infrastructure/    # Infrastructure Layer
│   │       ├── adapter/       # REST Controller, Persistence
│   │       └── config/        # Spring Configuration (CORS, etc.)
│   └── src/main/resources/
│       └── application.yml    # Database config
│
└── frontend/                   # React + TypeScript
    ├── src/
    │   ├── components/        # React Components (FeedCard, ArticleCard)
    │   ├── pages/             # Page Components (FeedsPage, ArticlesPage)
    │   ├── api/               # API Client (Axios)
    │   └── types/             # TypeScript Interfaces
    └── package.json
```

## 🚀 Quick Start

### Backend (Spring Boot)

```bash
# Verbinden
sshpass -p "password" ssh -p 2222 abc@host.docker.internal
cd /config/workspace/projects/java/news-aggregator

# Bauen
export JAVA_HOME=/config/data/User/globalStorage/pleiades.java-extension-pack-jdk/java/latest/
export PATH=$JAVA_HOME/bin:$PATH
mvn clean package -DskipTests -Dmaven.compiler.release=21

# Starten
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-arm64/
export PATH=$JAVA_HOME/bin:$PATH
java -jar target/news-aggregator-1.0.0.jar --spring.profiles.active=prod
```

**API läuft auf:** `http://localhost:8080`

### Frontend (React)

```bash
cd /config/workspace/projects/java/news-aggregator/frontend

# Dependencies installieren (nur erstmalig)
npm install

# Entwicklungsserver starten
npm run dev
```

**Frontend läuft auf:** `http://localhost:5173`

## 📚 Features

### Backend
- ✅ **REST API** - Vollständige CRUD-API für Feeds & Artikel
- ✅ **PostgreSQL Datenbank** - Persistent, produktionsbereit
- ✅ **CORS Support** - Für React Frontend-Zugriff
- ✅ **RSS/Atom Feed Aggregation** - Automatisches Abrufen
- ✅ **Media RSS Support** - Thumbnails aus `media:content`
- ✅ **Lombok** - Boilerplate-Reduktion
- ✅ **Hexagonale Architektur** - Clean Architecture

### Frontend
- ✅ **React 19** mit TypeScript
- ✅ **Vite** - Schnelles Build-Tool
- ✅ **Tailwind CSS** - Modernes Styling
- ✅ **React Router** - Client-Side Navigation
- ✅ **Axios** - API-Client
- ✅ **Responsive Design** - Mobile-freundlich

## 🔌 API Endpoints

| Methode | Endpoint | Beschreibung |
|---------|----------|--------------|
| GET | `/api/feeds` | Alle Feeds anzeigen |
| GET | `/api/feeds/{id}` | Einzelnen Feed anzeigen |
| POST | `/api/feeds` | Neuen Feed hinzufügen |
| POST | `/api/feeds/{id}/fetch` | Feed manuell abrufen |
| DELETE | `/api/feeds/{id}` | Feed löschen (inkl. Artikel) |
| GET | `/api/articles` | Alle Artikel anzeigen |
| GET | `/api/articles/{id}` | Einzelnen Artikel anzeigen |
| GET | `/api/articles/search?query=...` | Artikel suchen |
| GET | `/api/articles/feed/{feedId}` | Artikel eines Feeds |

## 🖥️ Frontend Pages

- **`/`** - Artikel-Übersicht mit Suche
- **`/feeds`** - Feed-Verwaltung (hinzufügen, löschen, aktualisieren)

## 📖 Technologien

### Backend
| Kategorie | Technologie |
|-----------|-------------|
| **Framework** | Spring Boot 3.2 |
| **Java** | 21 (Runtime), 25 (Build) |
| **Persistence** | Spring Data JPA |
| **Datenbank** | PostgreSQL (Prod), H2 (Dev) |
| **RSS Parsing** | Rome |
| **Build** | Maven |

### Frontend
| Kategorie | Technologie |
|-----------|-------------|
| **Framework** | React 19 |
| **Language** | TypeScript |
| **Build Tool** | Vite |
| **Styling** | Tailwind CSS |
| **Routing** | React Router |
| **HTTP Client** | Axios |
| **Icons** | Lucide React |

## 🗄️ Datenbank-Konfiguration

### PostgreSQL (Production)
```yaml
spring:
  profiles:
    active: prod
  datasource:
    url: jdbc:postgresql://${POSTGRES_HOST:host.docker.internal}:5432/newsdb
    username: ${POSTGRES_USER:postgres}
    password: ${POSTGRES_PASSWORD:postgres}
  jpa:
    hibernate:
      ddl-auto: update
```

### H2 (Development)
```yaml
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:h2:mem:newsdb
```

## 🔀 CORS Konfiguration

Das Backend erlaubt Zugriff vom React Dev-Server:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true);
    }
}
```

## 🧪 Tests

```bash
# Backend Tests
mvn test

# Frontend Tests (wenn konfiguriert)
cd frontend
npm test
```

## 📝 Umgebungsvariablen

### Backend (.env)
```
POSTGRES_HOST=host.docker.internal
POSTGRES_PORT=5432
POSTGRES_DB=newsdb
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
```

### Frontend (.env)
```
VITE_API_URL=http://localhost:8080/api
```

## 🗺️ Roadmap

Siehe [ROADMAP.md](ROADMAP.md) für geplante Features.

## 📄 Lizenz

MIT
