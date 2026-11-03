package com.civicvote.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtGatewayFilter implements GlobalFilter, Ordered {
    private final SecretKey key;
    public JwtGatewayFilter(@Value("${security.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    @Override public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path.equals("/api/auth/login") || path.equals("/api/auth/register") || path.equals("/actuator/health")) return chain.filter(exchange);
        String header = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return reject(exchange, HttpStatus.UNAUTHORIZED);
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(header.substring(7)).getPayload();
            ServerHttpRequest request = exchange.getRequest().mutate()
                .header("X-Authenticated-User", claims.getSubject())
                .header("X-Authenticated-Role", String.valueOf(claims.get("role")))
                .build();
            return chain.filter(exchange.mutate().request(request).build());
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException ex) {
            return reject(exchange, HttpStatus.UNAUTHORIZED);
        }
    }
    private Mono<Void> reject(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
    @Override public int getOrder() { return -100; }
}
