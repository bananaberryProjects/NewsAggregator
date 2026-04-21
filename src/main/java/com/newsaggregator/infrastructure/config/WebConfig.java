package com.newsaggregator.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                    "http://localhost:5173",
                    "http://127.0.0.1:5173",
                    "http://mac-mini.local:5173",
                    "http://172.20.0.2:5173"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Statische Frontend-Assets aus /app/static (Docker) oder classpath
        registry.addResourceHandler("/**")
                .addResourceLocations("file:/app/static/", "classpath:/static/")
                .setCachePeriod(3600);
    }

    @Override
    public void addViewControllers(@NonNull ViewControllerRegistry registry) {
        // Alle nicht-API Pfade leiten auf index.html um (Single Page Application)
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/{x:[\\w\\-]+}").setViewName("forward:/index.html");
        registry.addViewController("/{x:[\\w\\-]+}/**{y:[\\w\\-]+}").setViewName("forward:/index.html");
    }
}