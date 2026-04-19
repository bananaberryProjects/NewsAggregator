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
- [x] **Auto-Refresh** - Artikel automatisch alle X Minuten aktualisieren
- [x] **Ungelesen-Markierung** - Visueller Indikator für neue Artikel
- [x] **Bookmarking** - Artikel als "gelesen" oder "favorit" markieren
- [ ] **Infinite Scroll** - Statt Paginierung

### 🏷️ Organisation
- [x] **Kategorien/Tags** - Feeds und Artikel kategorisieren
- [ ] **Ordner/Collections** - Feeds in Ordner gruppieren
- [x] **Filter** - Nach Kategorie, Feed, Datum filtern
- [ ] **Priorisierung** - Wichtige Feeds hervorheben

### 🔍 Suche & Filter
- [ ] **Erweiterte Suche** - Mit Filtern (Datum, Feed, Kategorie)
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
- [ ] **Smart Fetch** - Häufigkeit pro Feed konfigurierbar
- [ ] **Auto-Cleanup** - Alte Artikel automatisch löschen (X Tage)
- [ ] **Duplicate Detection** - Doppelte Artikel erkennen und zusammenführen

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

## ✅ Bereits Implementiert

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
- [x] **OPML Import/Export**

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

*Letzte Aktualisierung: 2026-04-19*
