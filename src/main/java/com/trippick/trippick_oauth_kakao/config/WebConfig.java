package com.trippick.trippick_oauth_kakao.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                    "http://localhost:3000",    // React 기본 포트
                    "http://localhost:5173",    // Vite 기본 포트
                    "http://localhost:5174",    // Vite 대체 포트
                    "http://localhost:5175",    // Vite 대체 포트
                    "http://localhost:5176",    // Vite 대체 포트
                    "http://localhost:8080",    // 일반적인 개발 포트
                    "http://localhost:8081",    // 대체 포트
                    "http://localhost:8082",    // 대체 포트
                    "http://localhost:8083",    // 대체 포트
                    "http://localhost:8084",    // 대체 포트
                    "http://localhost:8085"     // 현재 Backend 포트
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
} 