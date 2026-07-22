package com.fireduty.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    private String secret = "fireduty-jwt-secret-change-in-production";
    private long expiration = 86400000; // 24 hours
}
