package com.mj.epayement.core.config;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.mj.epayement.core.properties.ApplicationProperties;

@Configuration
public class WebConfiguration {

    @Bean
    public CorsFilter corsFilter(ApplicationProperties properties) {
        var source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = properties.getCors();
        if (CollectionUtils.isNotEmpty(config.getAllowedOrigins())) {
            source.registerCorsConfiguration("/**", config);
        }
        return new CorsFilter(source);
    }

}
