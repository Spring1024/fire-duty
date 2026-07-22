package com.fireduty.gateway.filter;

import com.fireduty.gateway.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class JwtAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private final JwtConfig jwtConfig;

    public JwtAuthGatewayFilterFactory(JwtConfig jwtConfig) {
        super(Object.class);
        this.jwtConfig = jwtConfig;
    }

    @Override
    public String name() {
        return "JwtAuthGatewayFilter";
    }

    @Override
    public GatewayFilter apply(Object config) {
        return new OrderedGatewayFilter((exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();

            // Skip auth paths
            if (path.startsWith("/auth/login") || path.startsWith("/auth/refresh")
                    || path.startsWith("/health") || path.startsWith("/actuator")) {
                return chain.filter(exchange);
            }

            // Extract token
            String authHeader = exchange.getRequest().getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header for path: {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);
            try {
                SecretKey key = Keys.hmacShaKeyFor(
                        jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
                Jws<Claims> jws = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token);
                Claims claims = jws.getPayload();

                // Forward user info to downstream services via headers
                ServerWebExchange mutatedExchange = exchange.mutate()
                        .request(r -> r.headers(headers -> {
                            headers.set("X-User-Id", claims.get("userId", String.class));
                            headers.set("X-User-Name", claims.get("username", String.class));
                            headers.set("X-User-Role", claims.get("role", String.class));
                        }))
                        .build();
                return chain.filter(mutatedExchange);

            } catch (Exception e) {
                log.warn("JWT validation failed for path: {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        }, Ordered.LOWEST_PRECEDENCE);
    }
}
