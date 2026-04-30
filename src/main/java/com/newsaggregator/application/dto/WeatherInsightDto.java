package com.newsaggregator.application.dto;

import java.util.List;

/**
 * DTO fuer KI-generierten Wetter-Insight.
 */
public class WeatherInsightDto {

    private double temperature;
    private int weatherCode;
    private String description;
    private double todayMin;
    private double todayMax;
    private String city;
    private String insight;
    private List<ForecastDay> forecast;
    private String generatedAt;

    public WeatherInsightDto() {}

    public WeatherInsightDto(double temperature, int weatherCode, String description,
                             double todayMin, double todayMax, String city,
                             String insight, List<ForecastDay> forecast, String generatedAt) {
        this.temperature = temperature;
        this.weatherCode = weatherCode;
        this.description = description;
        this.todayMin = todayMin;
        this.todayMax = todayMax;
        this.city = city;
        this.insight = insight;
        this.forecast = forecast;
        this.generatedAt = generatedAt;
    }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public int getWeatherCode() { return weatherCode; }
    public void setWeatherCode(int weatherCode) { this.weatherCode = weatherCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getTodayMin() { return todayMin; }
    public void setTodayMin(double todayMin) { this.todayMin = todayMin; }

    public double getTodayMax() { return todayMax; }
    public void setTodayMax(double todayMax) { this.todayMax = todayMax; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getInsight() { return insight; }
    public void setInsight(String insight) { this.insight = insight; }

    public List<ForecastDay> getForecast() { return forecast; }
    public void setForecast(List<ForecastDay> forecast) { this.forecast = forecast; }

    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }

    public static class ForecastDay {
        private String day;
        private double maxTemp;
        private double minTemp;
        private int weatherCode;

        public ForecastDay() {}

        public ForecastDay(String day, double maxTemp, double minTemp, int weatherCode) {
            this.day = day;
            this.maxTemp = maxTemp;
            this.minTemp = minTemp;
            this.weatherCode = weatherCode;
        }

        public String getDay() { return day; }
        public void setDay(String day) { this.day = day; }

        public double getMaxTemp() { return maxTemp; }
        public void setMaxTemp(double maxTemp) { this.maxTemp = maxTemp; }

        public double getMinTemp() { return minTemp; }
        public void setMinTemp(double minTemp) { this.minTemp = minTemp; }

        public int getWeatherCode() { return weatherCode; }
        public void setWeatherCode(int weatherCode) { this.weatherCode = weatherCode; }
    }
}
