# 📰 News Aggregator

Ein Spring Boot basiertes News-Aggregationssystem mit hexagonaler Architektur, PostgreSQL-Datenbank, Thumbnail-Unterstützung und Lombok.

## 🏗️ Architektur

Das Projekt folgt der **Hexagonalen Architektur** (Clean Architecture / Ports and Adapters):

```
com.newsaggregator/
├── domain/              # Domain Layer - Reine Business-Logik
│   ├── model/           # Entities, Value Objects (Feed, Article, FeedId, ArticleId)
│   └── port/            # Interfaces (in/out)
│       ├── in/          # Use Case Interfaces
│       └── out/         # Repository Interfaces
├── application/         # Application Layer - Use Cases
│   ├── dto/             # Data Transfer Objects (Lombok @Data, @Builder)
│   ├── mapper/          # Domain ↔ DTO Mapping
│   └── service/         # Application Services
└── infrastructure/      # Infrastructure Layer
    ├── adapter/         # Implementierungen
    │   ├── persistence/ # JPA, PostgreSQL/H2
    │   ├── rss/         # RSS Feed Reader
    │   └── web/         # REST Controller, Thymeleaf Views
    └── config/          # Spring Configuration
```

## 🚀 Quick Start

### Voraussetzungen
- **Java:** Java 21 Runtime (Java 25 JDK kann für Build verwendet werden)
- **Maven:** 3.8+
- **Datenbank:** PostgreSQL (Prod) oder H2 (Dev)

### Build auf Code-Server

```bash
# Verbinden
sshpass -p "password" ssh -p 2222 abc@host.docker.internal
cd /config/workspace/projects/java/news-aggregator

# Bauen (mit Java 25 JDK, Ziel Java 21)
export JAVA_HOME=/config/data/User/globalStorage/pleiades.java-extension-pack-jdk/java/latest/
export PATH=$JAVA_HOME/bin:$PATH
mvn clean package -DskipTests -Dmaven.compiler.release=21

# Starten (mit Java 21 JRE)
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-arm64/
export PATH=$JAVA_HOME/bin:$PATH
java -jar target/news-aggregator-1.0.0.jar --spring.profiles.active=prod
```

### Zugriff

- **Web UI:** `http://host.docker.internal:7443/proxy/8080/`
- **Health:** `http://localhost:8080/actuator/health`

## 🧪 Tests

Das Projekt enthält umfassende Tests auf allen Ebenen:

| Layer | Testklasse | Typ |
|-------|------------|-----|
| Domain | FeedTest, ArticleTest, FeedIdTest, ArticleIdTest | Unit |
| Application | FeedManagementServiceTest, FeedFetchingServiceTest, ArticleSearchServiceTest | Unit (Mockito) |
| Infrastructure | FeedRepositoryAdapterIntegrationTest, ArticleRepositoryAdapterIntegrationTest | Integration |
| Infrastructure | FeedPersistenceMapperTest, ArticlePersistenceMapperTest | Unit |
| Infrastructure | FeedControllerTest, RssFeedReaderAdapterTest | Unit (Mockito) |

### Testausführung

```bash
# Alle Tests
mvn test

# Nur Unit-Tests (keine Integration)
mvn test -Dtest="*Test,*UnitTest" -DexcludedGroups="integration"

# Mit Coverage
mvn jacoco:report
```

## 📚 Features

- ✅ **RSS/Atom Feed Aggregation** - Automatisches Abrufen von RSS-Feeds
- ✅ **PostgreSQL Datenbank** - Persistent, produktionsbereit
- ✅ **H2 Dev-Datenbank** - In-Memory für Entwicklung
- ✅ **Thumbnail-Unterstützung** - Bilder aus RSS-Feeds extrahieren (Enclosures & img-Tags)
- ✅ **Lombok Integration** - Boilerplate-Reduktion (@Data, @Builder, etc.)
- ✅ **Automatisches Abrufen** - Jede Stunde (cron)
- ✅ **Manuelles Abrufen** - Über Web-UI oder REST API
- ✅ **Feed-Verwaltung** - Hinzufügen, Löschen (Cascade-Delete für Artikel)
- ✅ **Artikel-Suche** - Volltextsuche über Titel und Beschreibung
- ✅ **REST API** - JSON-API für alle Operationen
- ✅ **Web UI** - Thymeleaf-basierte Oberfläche
- ✅ **Hexagonale Architektur** - Clean Architecture mit Ports & Adapters
- ✅ **Reverse-Proxy kompatibel** - X-Forwarded-Prefix Header Support

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

## 🌐 Web UI

- **`/`** - Dashboard mit allen Feeds und Artikeln
- **`/feeds`** - Feed-Verwaltung (hinzufügen, löschen, aktualisieren)
- **`/articles`** - Alle Artikel mit Thumbnails
- **`/search?query=...`** - Volltextsuche

## 📖 Technologien

| Kategorie | Technologie |
|-----------|-------------|
| **Framework** | Spring Boot 3.2 |
| **Java** | 21 (Runtime), 25 (Build) |
| **Persistence** | Spring Data JPA |
| **Datenbank** | PostgreSQL (Prod), H2 (Dev) |
| **Template Engine** | Thymeleaf |
| **Boilerplate** | Lombok |
| **RSS Parsing** | Rome |
| **Testing** | JUnit 5, Mockito |
| **Build** | Maven |

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
    username: sa
    password:
```

## 🔧 Lombok Features

Verwendete Lombok-Annotationen:

- `@Data` - Getter, Setter, ToString, EqualsAndHashCode
- `@Builder` - Builder-Pattern für DTOs
- `@NoArgsConstructor` / `@AllArgsConstructor` - Konstruktoren
- `@Value` - Für unveränderliche Value Objects (ArticleId, FeedId)

**Beispiel Reduktion:**
- `ArticleDto`: ~150 Zeilen → ~25 Zeilen
- `ArticleJpaEntity`: ~140 Zeilen → ~50 Zeilen

## 🖼️ Thumbnail-Extraktion

Bilder werden aus RSS-Feeds automatisch extrahiert:

1. **RSS Enclosures** - `enclosure` Elemente mit `type="image/*"`
2. **IMG-Tags** - Regex-Extraktion aus HTML-Beschreibung
3. **Fallback** - Platzhalter 📰 wenn kein Bild verfügbar

## 🗑️ Cascade Delete

Beim Löschen eines Feeds über Web-UI oder API:
- **Warnung** im UI mit Anzahl der zu löschenden Artikel
- **Automatisches Löschen** aller zugehörigen Artikel
- Implementiert via `CascadeType.ALL` auf `@OneToMany` Beziehung

## 🔀 Reverse-Proxy Konfiguration

Für Betrieb hinter Reverse-Proxy (z.B. Code-Server):

**Header setzen:**
```
X-Forwarded-Prefix: /proxy/8080
```

**Thymeleaf Templates verwenden:**
```html
<a th:href="@{/feeds}">Feeds</a>  <!-- Wird zu /proxy/8080/feeds -->
```

## 📝 Aktive Feeds (Beispiel)

| Feed | Artikel | Kategorie |
|------|---------|-----------|
| Heise Online | ~156 | Tech 🇩🇪 |
| Golem.de | ~45 | Tech 🇩🇪 |
| Sportschau Fußball | ~64 | Fußball 🇩🇪 |
| Investing.com DE | ~14 | Finanzen 🇩🇪 |
| MoviePilot | ~21 | Kino 🇩🇪 |

## 📄 Lizenz

MIT
