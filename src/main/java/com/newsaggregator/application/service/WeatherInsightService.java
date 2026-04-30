package com.newsaggregator.application.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsaggregator.application.dto.WeatherInsightDto;
import com.newsaggregator.domain.model.Article;
import com.newsaggregator.domain.port.out.ArticleRepository;
import com.newsaggregator.infrastructure.adapter.persistence.entity.WeatherInsightCacheJpaEntity;
import com.newsaggregator.infrastructure.adapter.persistence.repository.WeatherInsightCacheJpaRepository;

import lombok.Setter;

/**
 * Service fuer KI-generierte Wetter-Einblicke.
 *
 * <p>Holt Wetterdaten von Open-Meteo, generiert einen persoenlichen
 * KI-Kommentar via Ollama und cached das Ergebnis.</p>
 */
@Service
public class WeatherInsightService {

    private static final Logger log = LoggerFactory.getLogger(WeatherInsightService.class);
    private static final Duration CACHE_TTL = Duration.ofMinutes(60);
    private static final String OPEN_METEO_URL = "https://api.open-meteo.com/v1/forecast";

    private final ArticleRepository articleRepository;
    private final WeatherInsightCacheJpaRepository cacheRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ollama.base-url:http://host.docker.internal:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.model:gpt-oss:120b-cloud}")
    private String ollamaModel;

    public WeatherInsightService(ArticleRepository articleRepository,
                                  WeatherInsightCacheJpaRepository cacheRepository) {
        this.articleRepository = articleRepository;
        this.cacheRepository = cacheRepository;
        this.restTemplate = createRestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(300000);
        return new RestTemplate(factory);
    }

    /**
     * Generiert einen KI-Wetter-Insight fuer einen Standort.
     */
    public WeatherInsightDto generateInsight(double lat, double lon, String cityName) {
        String locationKey = String.format(Locale.US, "%.4f,%.4f", lat, lon);
        LocalDateTime now = LocalDateTime.now();

        // 1. Cache pruefen
        Optional<WeatherInsightCacheJpaEntity> cached = cacheRepository.findByLocationKey(locationKey);
        if (cached.isPresent()) {
            WeatherInsightCacheJpaEntity entry = cached.get();
            if (entry.getExpiresAt().isAfter(now)) {
                log.debug("Weather-Cache-Hit fuer {}", locationKey);
                try {
                    WeatherInsightDto dto = objectMapper.readValue(entry.getInsightJson(), WeatherInsightDto.class);
                    if (dto.getInsight() != null && !dto.getInsight().trim().isEmpty()) {
                        log.info("Wetter-Insight aus Cache ({} Zeichen)", dto.getInsight().length());
                        return dto;
                    }
                    log.warn("Gecachter Wetter-Insight war leer, wird neu generiert");
                } catch (Exception e) {
                    log.warn("Wetter-Cache kaputt, wird neu generiert: {}", e.getMessage());
                }
            }
        }

        // 2. Wetterdaten von Open-Meteo holen
        WeatherData weather = fetchOpenMeteo(lat, lon);

        // 3. KI-Insight generieren
        WeatherInsightDto result;
        boolean isFallback = false;
        try {
            String insight = callOllamaInsight(weather, cityName);
            weather.setInsight(insight);
            result = buildDto(weather, cityName);
        } catch (Exception e) {
            log.warn("KI-Wetter-Insight fehlgeschlagen, Fallback: {}", e.getMessage());
            weather.setInsight(buildFallbackInsight(weather, cityName));
            result = buildDto(weather, cityName);
            isFallback = true;
        }

        // 4. Cachen
        saveToCache(locationKey, result, isFallback);
        return result;
    }

    private WeatherData fetchOpenMeteo(double lat, double lon) {
        String url = String.format(Locale.US,
            "%s?latitude=%.4f&longitude=%.4f&current=temperature_2m,weather_code&daily=temperature_2m_max,temperature_2m_min,weather_code&forecast_days=5&timezone=auto",
            OPEN_METEO_URL, lat, lon);

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            JsonNode current = root.path("current");
            JsonNode daily = root.path("daily");

            WeatherData data = new WeatherData();
            data.temperature = current.path("temperature_2m").asDouble();
            data.weatherCode = current.path("weather_code").asInt();
            data.todayMin = daily.path("temperature_2m_min").get(0).asDouble();
            data.todayMax = daily.path("temperature_2m_max").get(0).asDouble();

            List<WeatherInsightDto.ForecastDay> forecast = new ArrayList<>();
            JsonNode times = daily.path("time");
            JsonNode maxTemps = daily.path("temperature_2m_max");
            JsonNode minTemps = daily.path("temperature_2m_min");
            JsonNode codes = daily.path("weather_code");

            String[] days = {"So","Mo","Di","Mi","Do","Fr","Sa"};
            for (int i = 0; i < times.size(); i++) {
                String dateStr = times.get(i).asText();
                java.time.LocalDate d = java.time.LocalDate.parse(dateStr);
                String dayName = days[d.getDayOfWeek().getValue() % 7];
                forecast.add(new WeatherInsightDto.ForecastDay(
                    dayName,
                    maxTemps.get(i).asDouble(),
                    minTemps.get(i).asDouble(),
                    codes.get(i).asInt()
                ));
            }
            data.forecast = forecast;
            return data;
        } catch (Exception e) {
            throw new RuntimeException("Open-Meteo Fehler: " + e.getMessage(), e);
        }
    }

    private String callOllamaInsight(WeatherData weather, String cityName) throws Exception {
        // Ollama erreichbar?
        try {
            restTemplate.getForEntity(ollamaBaseUrl + "/api/tags", String.class);
        } catch (Exception ex) {
            throw new RuntimeException("Ollama nicht erreichbar: " + ex.getMessage());
        }

        // News-Kontext holen
        List<Article> recentArticles = articleRepository.findAll().stream()
                .sorted(Comparator.comparing(Article::getPublishedAt).reversed())
                .limit(3)
                .toList();
        String newsContext = recentArticles.stream()
                .map(Article::getTitle)
                .collect(Collectors.joining("; "));

        StringBuilder prompt = new StringBuilder();
        prompt.append("Du bist ein freundlicher Wetter-Assistent. Schreibe einen kurzen, persoenlichen Wetterkommentar auf Deutsch (2-3 Saetze).\n\n");
        prompt.append("Aktuelles Wetter in ").append(cityName).append(":\n");
        prompt.append("- Temperatur: ").append(Math.round(weather.temperature)).append("°C\n");
        prompt.append("- Beschreibung: ").append(getWeatherDescription(weather.weatherCode)).append("\n");
        prompt.append("- Heute min/max: ").append(Math.round(weather.todayMin)).append("°C / ").append(Math.round(weather.todayMax)).append("°C\n");
        prompt.append("- Tageszeit: ").append(getDayPhase()).append("\n");

        if (!newsContext.isEmpty()) {
            prompt.append("\nAktuelle News-Headlines: ").append(newsContext).append("\n");
            prompt.append("Beziehe dich OPTIONAL auf eine passende News, wenn sie zum Wetter passt (z.B. guter Tag fuer Outdoor-News).\n");
        }

        prompt.append("\nDer Kommentar soll:\n");
        prompt.append("- Persoenlich und locker sein\n");
        prompt.append("- Eine konkrete Empfehlung geben (Kleidung, Aktivitaet, etc.)\n");
        prompt.append("- Die Forecast beruecksichtigen\n");
        prompt.append("- Maximal 200 Zeichen\n");
        prompt.append("Antworte AUSSCHLIESSLICH mit dem reinen Text (kein JSON, kein Markdown).\n");

        Map<String, Object> options = new HashMap<>();
        options.put("temperature", 0.7);
        options.put("num_predict", 512);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", ollamaModel);
        requestBody.put("prompt", prompt.toString());
        requestBody.put("stream", false);
        requestBody.put("options", options);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        String response = restTemplate.postForObject(ollamaBaseUrl + "/api/generate", request, String.class);
        log.debug("Ollama raw response: {}", response);
        JsonNode jsonNode = objectMapper.readTree(response);
        String insight = jsonNode.path("response").asText("").trim();
        if (insight.isEmpty()) {
            log.warn("Ollama lieferte leeren Insight! Full JSON: {}", response);
            throw new RuntimeException("Ollama Insight war leer");
        }
        // Remove <think> tags if present (common with reasoning models)
        insight = insight.replaceAll("<think>.*?</think>", "").trim();
        // Normalize whitespace
        insight = insight.replaceAll("\\s+", " ");
        if (insight.length() > 250) {
            insight = insight.substring(0, 247) + "...";
        }
        log.info("KI-Insight generiert ({} Zeichen): {}", insight.length(), insight);
        return insight;
    }

    private String buildFallbackInsight(WeatherData weather, String cityName) {
        String desc = getWeatherDescription(weather.weatherCode);
        int temp = (int) Math.round(weather.temperature);
        String phase = getDayPhase();

        if (weather.weatherCode >= 61) {
            return phase + " in " + cityName + ": " + temp + "°C und " + desc.toLowerCase() +
                   ". Nimm den Regenschirm mit!";
        }
        if (weather.weatherCode == 0 && temp >= 20) {
            return phase + " in " + cityName + ": " + temp + "°C bei klarem Himmel. Geniesse den Tag!";
        }
        return phase + " in " + cityName + ": " + temp + "°C, " + desc.toLowerCase() +
               ". Heute wird es zwischen " + Math.round(weather.todayMin) + "° und " + Math.round(weather.todayMax) + "°.";
    }

    private WeatherInsightDto buildDto(WeatherData weather, String cityName) {
        String generatedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return new WeatherInsightDto(
                weather.temperature,
                weather.weatherCode,
                getWeatherDescription(weather.weatherCode),
                weather.todayMin,
                weather.todayMax,
                cityName,
                weather.insight,
                weather.forecast,
                generatedAt
        );
    }

    private void saveToCache(String locationKey, WeatherInsightDto dto, boolean isFallback) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            WeatherInsightCacheJpaEntity entry = new WeatherInsightCacheJpaEntity();
            entry.setLocationKey(locationKey);
            entry.setInsightJson(json);
            entry.setFallback(isFallback);
            entry.setCreatedAt(LocalDateTime.now());
            entry.setExpiresAt(LocalDateTime.now().plus(CACHE_TTL));

            cacheRepository.findByLocationKey(locationKey).ifPresent(existing -> {
                entry.setId(existing.getId());
            });
            cacheRepository.save(entry);
            log.info("Wetter-Insight gecacht fuer {} (fallback={})", locationKey, isFallback);
        } catch (Exception e) {
            log.warn("Wetter-Insight cachen fehlgeschlagen: {}", e.getMessage());
        }
    }

    private String getDayPhase() {
        int hour = LocalDateTime.now().getHour();
        if (hour < 6) return "Nacht";
        if (hour < 12) return "Morgen";
        if (hour < 18) return "Nachmittag";
        return "Abend";
    }

    /**
     * Invalidiert den gesamten Wetter-Insight-Cache.
     */
    public void invalidateCache() {
        cacheRepository.deleteAll();
        log.info("Wetter-Insight-Cache vollstaendig invalidiert");
    }

    private static String getWeatherDescription(int code) {
        if (code == 0) return "Klarer Himmel";
        if (code == 1) return "Hauptsaechlich klar";
        if (code == 2) return "Teilweise bewoelkt";
        if (code == 3) return "Bedeckt";
        if (code >= 45 && code <= 48) return "Nebelig";
        if (code >= 51 && code <= 55) return "Nieselregen";
        if (code >= 61 && code <= 65) return "Regen";
        if (code >= 71 && code <= 77) return "Schneefall";
        if (code >= 80 && code <= 82) return "Regenschauer";
        if (code >= 95) return "Gewitter";
        return "Unbekannt";
    }

    @Setter
    private static class WeatherData {
        double temperature;
        int weatherCode;
        double todayMin;
        double todayMax;
        String insight;
        List<WeatherInsightDto.ForecastDay> forecast;
    }
}
