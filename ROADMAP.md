# 🗺️ News Aggregator Roadmap

Feature-Ideen und zukünftige Entwicklungen für das News Aggregator Projekt.

## 🚀 Geplante Features

### 📧 Benachrichtigungen
- [ ] **Email-Benachrichtigungen** - Bei neuen Artikeln in bestimmten Feeds
- [ ] **Push-Notifications** - Browser-Push für wichtige Artikel
- [ ] **Digest-Modus** - Tägliche Zusammenfassung per Email

### 🎨 UI/UX Verbesserungen
- [x] **Dark Mode** - Für die Web-UI (automatisch/optional)
- [x] **Dashboard Widgets** - Wetter, Börsenkurse, KI-Zusammenfassung
- [x] **Morning Briefing Dashboard Widget** - Zeitabhängiger Begrüßungs-Header mit personalisierten Lesestats (Count-Up Animationen, Lesestreak-Badge, Quick-Actions)
- [x] **Auto-Refresh** - Artikel automatisch alle X Minuten aktualisieren
- [x] **Ungelesen-Markierung** - Visueller Indikator für neue Artikel
- [x] **Bookmarking** - Artikel als "gelesen" oder "favorit" markieren
- [x] **Infinite Scroll** - Automatisches Nachladen mit IntersectionObserver API

### 🏷️ Organisation
- [x] **Kategorien/Tags** - Feeds und Artikel kategorisieren
- [ ] **Ordner/Collections** - Feeds in Ordner gruppieren
- [x] **Filter** - Nach Kategorie, Feed, Datum filtern
- [ ] **Priorisierung** - Wichtige Feeds hervorheben

### 🔍 Suche & Filter
- [x] **Erweiterte Suche** - PostgreSQL Full-Text-Search mit tsvector-GIN-Index, Ranking, Filtern (Kategorie, Lese-/Favoriten-Status) und Paginierung
- [ ] **Such-Historie** - Letzte Suchen speichern
- [ ] **Gespeicherte Suchanfragen** - "Smarte Ordner"

### 📊 Statistiken & Analytics
- [x] **Dashboard mit Charts** - Artikel pro Feed, Zeitverlauf
- [x] **Lesestatistiken** - Meistgelesene Artikel, aktive Feeds
- [ ] **Export-Funktion** - Artikel als PDF/CSV exportieren

### 📤 Import/Export
- [x] **OPML Import/Export** - Feed-Abonnements sichern und wiederherstellen
- [ ] **Artikel-Export** - Artikel als PDF/CSV exportieren

### 🤖 KI & Automatisierung
- [x] **KI-Zusammenfassung** - Tägliche Zusammenfassung mit Ollama
- [x] **KI-Zusammenfassung 2.0** - Strukturierte Kategorien-Summary mit Sentiment, Accordion, TTS und Top-Themen
- [ ] **Smart Fetch** - Häufigkeit pro Feed konfigurierbar
- [x] **Auto-Cleanup** - Alte Artikel automatisch löschen (X Tage)
- [x] **Keyword-Filter pro Feed** - Blockierte Keywords pro Feed konfigurieren (Frontend & Backend)
- [x] **Duplicate Detection** - Titel-basierte Deduplizierung mit Normalisierung & Wortstamm-Matching
- [ ] **Headless Browser** - Für Artikel mit Bot-Schutz (Cloudflare, etc.) via Selenium/Playwright

### 🔐 Authentifizierung & Multi-User
- [ ] **User-Accounts** - Mehrere Benutzer mit eigenen Feeds
- [ ] **Rollen/Rechte** - Admin, Editor, Viewer
- [ ] **OAuth-Login** - Mit Google, GitHub, etc.

### 📱 Mobile & API
- [ ] **Responsive Design** - Mobile-Optimierung verbessern
- [ ] **REST API Erweiterung** - Mehr Endpoints, Pagination
- [ ] **GraphQL** - Alternative API
- [ ] **Mobile App** - PWA oder native App

### 🧪 Testing & Qualität
- [ ] **E2E Tests** - Mit Playwright oder Selenium
- [ ] **Performance Tests** - Load Testing
- [ ] **Code Coverage** - Mindestens 80%

### 🛠️ DevOps
- [ ] **Docker Compose** - Für einfaches Deployment
- [ ] **GitHub Actions** - CI/CD Pipeline
- [ ] **Automatische Updates** - Für Abhängigkeiten

--

- [x] **Hexagonale Architektur**
- [x] **PostgreSQL Datenbank**
- [x] **H2 Dev-Datenbank**
- [x] **Lombok Integration**
- [x] **Thumbnail-Extraktion**
- [x] **Feed-Verwaltung** (Hinzufügen, Löschen)
- [x] **Cascade-Delete** für Artikel
- [x] **Artikelanzahl pro Feed**
- [x] **Volltextsuche**
- [x] **Reverse-Proxy Support**
- [x] **Comprehensive Tests**
- [x] **Dashboard Widgets** (Wetter, Börsenkurse, KI-Zusammenfassung)
- [x] **Morning Briefing Dashboard Widget** - Zeitabhängiger Begrüßungs-Header mit Lesestats, Count-Up Animationen und Quick-Actions
- [x] **KI-Zusammenfassung 2.0** - Strukturierte Kategorien-Summary mit Sentiment, Accordion, TTS und Top-Themen
- [x] **OPML Import/Export**
- [x] **PWA Support** - Progressive Web App mit Offline-Cache und App-Installation
- [x] **Crypto Price Widget** - Live-Kryptopreis-Tracking über CoinGecko API (Bitcoin, Ethereum, Solana)
- [x] **Keyword-Filter pro Feed** - Blockierte Keywords pro Feed konfigurieren (Frontend & Backend)
- [x] **Duplicate Detection** - Titel-basierte Deduplizierung mit Normalisierung & Wortstamm-Matching

## 📝 Notizen

- Features können parallel entwickelt werden (Feature-Branches)
- Priorität basiert auf Nutzen vs. Aufwand
- Community-Feedback berücksichtigen

## 🤝 Mitwirken

Für neue Feature-Vorschläge:
1. Feature-Branch erstellen: `git checkout -b feature/feature-name`
2. Implementieren
3. Pull Request erstellen
4. Review & Merge

---

*Letzte Aktualisierung: 2026-04-29*
