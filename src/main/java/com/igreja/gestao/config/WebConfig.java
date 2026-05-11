package com.igreja.gestao.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mapeia a URL /uploads/** para a pasta física C:/igreja/uploads/
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///C:/igreja/uploads/");
    }
}