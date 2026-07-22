package com.fireduty.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /**
     * JWT签名密钥（Base64编码）
     */
    private String secret;

    /**
     * 访问令牌过期时间（毫秒，默认24小时）
     */
    private long expiration = 86400000L;

    /**
     * 刷新令牌过期时间（毫秒，默认7天）
     */
    private long refreshExpiration = 604800000L;

    /**
     * 签发者
     */
    private String issuer = "fire-duty-auth";
}
