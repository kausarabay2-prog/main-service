package com.bitlab.main_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI mainServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("BITLAB ACADEMY LMS - Main Service API")
                .description("Courses, chapters and lessons. Hierarchy: Course -> Chapter -> Lesson.")
                .version("1.0"));
    }
}