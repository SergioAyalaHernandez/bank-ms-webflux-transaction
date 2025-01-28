package com.example.transactionalms.config.token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(-1)
public class TokenForwardingFilter implements WebFilter {
    private final RequestTokenHolder tokenHolder;
    private static final Logger log = LoggerFactory.getLogger(TokenForwardingFilter.class);

    public TokenForwardingFilter(RequestTokenHolder tokenHolder) {
        this.tokenHolder = tokenHolder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui.html") || path.startsWith("/webjars/swagger-ui")) {
            return chain.filter(exchange);
        }
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.info("Token captured in filter: {}", token);
            return tokenHolder.withToken(token, chain.filter(exchange));
        }
        return chain.filter(exchange);
    }
}
