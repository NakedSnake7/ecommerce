package com.ecomerce.store.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // ============================
    // Soporte PUT / DELETE en forms
    // ============================
    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }

    // ============================
    // Ignorar /.well-known/**
    // (Chrome DevTools / PWA)
    // ============================
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/.well-known/**")
                .addResourceLocations("classpath:/static/.well-known/");
    }
    
    
}
