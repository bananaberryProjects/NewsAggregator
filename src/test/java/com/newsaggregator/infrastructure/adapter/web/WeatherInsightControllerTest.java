package com.newsaggregator.infrastructure.adapter.web;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsaggregator.application.dto.WeatherInsightDto;
import com.newsaggregator.application.service.WeatherInsightService;

/**
 * Unit-Test für WeatherInsightController.
 *
 * <p>Testet die REST-Endpunkte für Wetter-Einblicke.
 * Verwendet MockitoExtension statt @WebMvcTest, um Probleme mit Java 25 zu vermeiden.</p>
 */
@ExtendWith(MockitoExtension.class)
class WeatherInsightControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private WeatherInsightService weatherInsightService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        WeatherInsightController controller = new WeatherInsightController(weatherInsightService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getWeatherInsight_WithDefaultParameters_ShouldReturnBerlinWeather() throws Exception {
        // Given
        WeatherInsightDto weather = createWeatherInsightDto("Berlin", 22.0, 0, "Klarer Himmel");

        when(weatherInsightService.generateInsight(52.52, 13.41, "Berlin")).thenReturn(weather);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/weather/insight")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        WeatherInsightDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), WeatherInsightDto.class);
        assertThat(responseBody.getCity()).isEqualTo("Berlin");
        assertThat(responseBody.getTemperature()).isEqualTo(22.0);
        assertThat(responseBody.getWeatherCode()).isEqualTo(0);
        assertThat(responseBody.getDescription()).isEqualTo("Klarer Himmel");
    }

    @Test
    void getWeatherInsight_WithCustomParameters_ShouldReturnCustomLocation() throws Exception {
        // Given
        WeatherInsightDto weather = createWeatherInsightDto("Muenchen", 25.0, 1, "Hauptsaechlich klar");

        when(weatherInsightService.generateInsight(48.14, 11.58, "Muenchen")).thenReturn(weather);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/weather/insight")
                .param("lat", "48.14")
                .param("lon", "11.58")
                .param("city", "Muenchen")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        WeatherInsightDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), WeatherInsightDto.class);
        assertThat(responseBody.getCity()).isEqualTo("Muenchen");
        assertThat(responseBody.getTemperature()).isEqualTo(25.0);
    }

    @Test
    void getWeatherInsight_WithRainyWeather_ShouldReturnRainDescription() throws Exception {
        // Given - Weather code 61 = rain
        WeatherInsightDto weather = createWeatherInsightDto("Berlin", 15.0, 61, "Regen");
        weather.setInsight("Nimm den Regenschirm mit!");

        when(weatherInsightService.generateInsight(52.52, 13.41, "Berlin")).thenReturn(weather);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/weather/insight")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        WeatherInsightDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), WeatherInsightDto.class);
        assertThat(responseBody.getDescription()).isEqualTo("Regen");
        assertThat(responseBody.getInsight()).contains("Regenschirm");
    }

    @Test
    void getWeatherInsight_WithColdWeather_ShouldReturnLowTemperature() throws Exception {
        // Given
        WeatherInsightDto weather = createWeatherInsightDto("Berlin", -5.0, 71, "Schneefall");
        weather.setTodayMin(-8.0);
        weather.setTodayMax(-2.0);

        when(weatherInsightService.generateInsight(52.52, 13.41, "Berlin")).thenReturn(weather);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/weather/insight")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        WeatherInsightDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), WeatherInsightDto.class);
        assertThat(responseBody.getTemperature()).isEqualTo(-5.0);
        assertThat(responseBody.getTodayMin()).isEqualTo(-8.0);
        assertThat(responseBody.getTodayMax()).isEqualTo(-2.0);
    }

    @Test
    void getWeatherInsight_WithForecast_ShouldReturnFiveDayForecast() throws Exception {
        // Given
        WeatherInsightDto weather = createWeatherInsightDto("Berlin", 20.0, 0, "Klarer Himmel");
        weather.setForecast(List.of(
                new WeatherInsightDto.ForecastDay("Mo", 22.0, 15.0, 0),
                new WeatherInsightDto.ForecastDay("Di", 23.0, 16.0, 1),
                new WeatherInsightDto.ForecastDay("Mi", 21.0, 14.0, 2),
                new WeatherInsightDto.ForecastDay("Do", 19.0, 13.0, 3),
                new WeatherInsightDto.ForecastDay("Fr", 18.0, 12.0, 61)
        ));

        when(weatherInsightService.generateInsight(52.52, 13.41, "Berlin")).thenReturn(weather);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/weather/insight")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        WeatherInsightDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), WeatherInsightDto.class);
        assertThat(responseBody.getForecast()).hasSize(5);
        assertThat(responseBody.getForecast().get(0).getDay()).isEqualTo("Mo");
        assertThat(responseBody.getForecast().get(4).getDay()).isEqualTo("Fr");
    }

    @Test
    void getWeatherInsight_ResponseShouldContainAllFields() throws Exception {
        // Given
        WeatherInsightDto weather = createWeatherInsightDto("Berlin", 22.0, 0, "Klarer Himmel");
        weather.setTodayMin(18.0);
        weather.setTodayMax(25.0);
        weather.setInsight("Schoener Tag!");
        weather.setGeneratedAt(LocalDateTime.now().toString());

        when(weatherInsightService.generateInsight(52.52, 13.41, "Berlin")).thenReturn(weather);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/weather/insight")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        String json = result.getResponse().getContentAsString();
        assertThat(json).contains("temperature");
        assertThat(json).contains("weatherCode");
        assertThat(json).contains("description");
        assertThat(json).contains("todayMin");
        assertThat(json).contains("todayMax");
        assertThat(json).contains("city");
        assertThat(json).contains("insight");
        assertThat(json).contains("forecast");
        assertThat(json).contains("generatedAt");
    }

    @Test
    void getWeatherInsight_WithNullInsight_ShouldReturnEmptyInsight() throws Exception {
        // Given
        WeatherInsightDto weather = createWeatherInsightDto("Berlin", 22.0, 0, "Klarer Himmel");
        weather.setInsight(null);

        when(weatherInsightService.generateInsight(52.52, 13.41, "Berlin")).thenReturn(weather);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/weather/insight")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        WeatherInsightDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), WeatherInsightDto.class);
        assertThat(responseBody.getInsight()).isNull();
    }

    @Test
    void getWeatherInsight_WithEmptyInsight_ShouldReturnEmptyString() throws Exception {
        // Given
        WeatherInsightDto weather = createWeatherInsightDto("Berlin", 22.0, 0, "Klarer Himmel");
        weather.setInsight("");

        when(weatherInsightService.generateInsight(52.52, 13.41, "Berlin")).thenReturn(weather);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/weather/insight")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        WeatherInsightDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), WeatherInsightDto.class);
        assertThat(responseBody.getInsight()).isEmpty();
    }

    @Test
    void invalidateCache_ShouldReturnSuccessMessage() throws Exception {
        // Given
        doNothing().when(weatherInsightService).invalidateCache();

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(post("/api/weather/insight/invalidate")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(result.getResponse().getContentAsString()).contains("Cache invalidiert");
        verify(weatherInsightService).invalidateCache();
    }

    @Test
    void invalidateCache_ShouldReturnJsonResponse() throws Exception {
        // Given
        doNothing().when(weatherInsightService).invalidateCache();

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(post("/api/weather/insight/invalidate")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(result.getResponse().getContentAsString()).contains("message");
    }

    @Test
    void getWeatherInsight_WithSpecialCharactersInCity_ShouldHandleCorrectly() throws Exception {
        // Given
        WeatherInsightDto weather = createWeatherInsightDto("Zürich", 20.0, 0, "Klarer Himmel");

        when(weatherInsightService.generateInsight(47.38, 8.54, "Zürich")).thenReturn(weather);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/weather/insight")
                .param("lat", "47.38")
                .param("lon", "8.54")
                .param("city", "Zürich")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        WeatherInsightDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), WeatherInsightDto.class);
        assertThat(responseBody.getCity()).isEqualTo("Zürich");
    }

    @Test
    void getWeatherInsight_ShouldIncludeGeneratedAtTimestamp() throws Exception {
        // Given
        String now = LocalDateTime.now().toString();
        WeatherInsightDto weather = createWeatherInsightDto("Berlin", 22.0, 0, "Klarer Himmel");
        weather.setGeneratedAt(now);

        when(weatherInsightService.generateInsight(52.52, 13.41, "Berlin")).thenReturn(weather);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/weather/insight")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        WeatherInsightDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), WeatherInsightDto.class);
        assertThat(responseBody.getGeneratedAt()).isEqualTo(now);
    }

    private WeatherInsightDto createWeatherInsightDto(String city, double temperature, int weatherCode, String description) {
        WeatherInsightDto weather = new WeatherInsightDto();
        weather.setTemperature(temperature);
        weather.setWeatherCode(weatherCode);
        weather.setDescription(description);
        weather.setTodayMin(temperature - 4);
        weather.setTodayMax(temperature + 3);
        weather.setCity(city);
        weather.setInsight("Schoenes Wetter in " + city + "!");
        weather.setForecast(List.of());
        weather.setGeneratedAt(LocalDateTime.now().toString());
        return weather;
    }
}
