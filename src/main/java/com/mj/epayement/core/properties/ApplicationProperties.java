package com.mj.epayement.core.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@ConfigurationProperties(prefix = "application")
@Getter
@Setter
public class ApplicationProperties {
    private final CorsConfiguration cors = new CorsConfiguration();

    private final Cors customCors  = new Cors();

    @Getter
    @Setter
    public static class Cors {
        private String allowedOrigin;
    }

}
