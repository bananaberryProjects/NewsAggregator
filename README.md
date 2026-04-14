# 📰 News Aggregator

Ein Spring Boot basiertes News-Aggregationssystem mit hexagonaler Architektur, PostgreSQL-Datenbank und **React + Material UI Frontend**.

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
└── frontend/                   # React + TypeScript + Material UI
    ├── src/
    │   ├── api/               # API Client
    │   └── App.tsx            # Main App Component
    └── package.json
```

## 🚀 Quick Start

### Backend (Spring Boot)

```bash
# Verbinden
sshpass -p "password" ssh -p 2222 coder@host.docker.internal
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

### Frontend (React + Material UI)

```bash
cd /config/workspace/projects/java/news-aggregator/frontend

# Entwicklungsserver starten
npx vite --host 0.0.0.0
```

**Frontend läuft auf:** `http://localhost:5173`

**Zugriff via Code-Server:** `http://mac-mini.local:5173/`

## ✨ Features

### Backend
- ✅ **REST API** - Vollständige CRUD-API für Feeds & Artikel
- ✅ **PostgreSQL Datenbank** - Persistent, produktionsbereit
- ✅ **CORS Support** - Für Frontend-Zugriff (localhost:5173, mac-mini.local:5173)
- ✅ **RSS/Atom Feed Aggregation** - Automatisches Abrufen
- ✅ **Media RSS Support** - Thumbnails aus `media:content`
- ✅ **Lombok** - Boilerplate-Reduktion
- ✅ **Hexagonale Architektur** - Clean Architecture
- ✅ **Cascade Delete** - Löschen eines Feeds löscht alle Artikel
- ✅ **Artikel Status** - Lesen/Favoriten verwalten (PostgreSQL)

### Frontend
- ✅ **React 19** mit TypeScript
- ✅ **Vite** - Schnelles Build-Tool
- ✅ **Material UI (MUI)** - Modernes, responsives Design
- ✅ **Sidebar Navigation** - Dashboard / Feeds / Artikel / Favoriten
- ✅ **Feed Management** - Hinzufügen, Löschen, Aktualisieren
- ✅ **Artikel-Übersicht** - Einheitliche Karten (480x400px)
- ✅ **Suche** - Artikel durchsuchen
- ✅ **Filter** - Alle / Ungelesen / Favoriten
- ✅ **Alphabetische Sortierung** - Feeds automatisch sortiert
- ✅ **Scrollbar Feed-Liste** - Alle Feeds in Sidebar sichtbar
- ✅ **Lokale Placeholder** - Keine externen Bild-Abhängigkeiten
- ✅ **Dark/Light Mode** - Theme-Umschaltung
- ✅ **Interaktive Icons** - Lesen/Gelesen markieren, Favoriten verwalten
- ✅ **Datum-Anzeige** - In Artikel-Kacheln mit Kalender-Icon
- ✅ **Dashboard-Limit** - Maximal 10 neueste Artikel, nach Datum sortiert
- ✅ **Verbesserte Textdarstellung** - Natürliche Beschreibungs-Umbrüche

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
| POST | `/api/articles/{id}/read` | Als gelesen markieren |
| POST | `/api/articles/{id}/unread` | Als ungelesen markieren |
| POST | `/api/articles/{id}/favorite` | Favorit toggeln |
| GET | `/api/articles/{id}/status` | Status abrufen |
| GET | `/api/articles/read` | Alle gelesenen Artikel |
| GET | `/api/articles/favorites` | Alle Favoriten |

## 🖥️ Frontend Views

- **🏠 Dashboard** - Statistik-Karten + Neueste 10 Artikel mit Filter (Alle/Ungelesen/Favoriten)
- **📰 Feeds** - Alle Feeds mit Refresh/Delete-Buttons
- **📄 Artikel** - Alle Artikel mit Filter (Alle/Ungelesen/Favoriten) + Interaktions-Icons
- **⭐ Favoriten** - Alle als Favorit markierte Artikel

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
| **UI Library** | Material UI (MUI) |
| **Icons** | Material Icons |

## 🗄️ Datenbank-Struktur

### Tabellen

**feeds** - RSS/Atom Feeds
| Feld | Typ | Beschreibung |
|------|-----|--------------|
| id | BIGINT PK | Eindeutige ID |
| name | VARCHAR | Feed-Name |
| url | VARCHAR | Feed-URL |
| description | VARCHAR | Beschreibung |
| image_url | VARCHAR | Feed-Logo |
| created_at | TIMESTAMP | Erstellungsdatum |

**articles** - Artikel aus Feeds
| Feld | Typ | Beschreibung |
|------|-----|--------------|
| id | BIGINT PK | Eindeutige ID |
| title | VARCHAR | Artikel-Titel |
| description | TEXT | Artikel-Text |
| link | VARCHAR | Original-URL |
| image_url | VARCHAR | Thumbnail |
| published_at | TIMESTAMP | Veröffentlichung |
| feed_id | BIGINT FK | Referenz zu Feed |

**article_read_status** - Lesen/Favoriten Status
| Feld | Typ | Beschreibung |
|------|-----|--------------|
| id | BIGINT PK | Eindeutige ID |
| article_id | BIGINT FK | Referenz zu Artikel |
| user_id | VARCHAR | User-ID (für Multi-User) |
| is_read | BOOLEAN | Gelesen? |
| is_favorite | BOOLEAN | Favorit? |
| read_at | TIMESTAMP | Wann gelesen |
| favorited_at | TIMESTAMP | Wann favorisiert |

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

Das Backend erlaubt Zugriff vom Frontend:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                    "http://localhost:5173",
                    "http://mac-mini.local:5173"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
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
VITE_API_URL=/api
```

**Proxy-Konfiguration** (vite.config.ts):
```typescript
proxy: {
  '/api': {
    target: 'http://host.docker.internal:7443/proxy/8080',
    changeOrigin: true,
    secure: false
  }
}
```

## 🗺️ Roadmap

- [x] 🔖 Artikel als "gelesen" markieren
- [x] ⭐ Artikel favorisieren
- [x] 🔍 Filter (Alle / Ungelesen / Favoriten)
- [ ] 📱 PWA Support
- [ ] 🔔 Push-Benachrichtigungen
- [ ] 📊 Statistiken über Lesegewohnheiten
- [ ] 🏷️ Tags / Kategorien für Feeds
- [ ] 📤 OPML Import/Export

## 📄 Lizenz

MIT
