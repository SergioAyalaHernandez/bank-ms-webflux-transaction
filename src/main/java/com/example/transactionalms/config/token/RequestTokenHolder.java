package com.example.transactionalms.config.token;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RequestTokenHolder {
    private static final String TOKEN_KEY = "AUTH_TOKEN";

    public Mono<String> getToken() {
        return Mono.deferContextual(ctx ->
                ctx.hasKey(TOKEN_KEY) ? Mono.just(ctx.get(TOKEN_KEY)) : Mono.empty()
        );
    }

    public <T> Mono<T> withToken(String token, Mono<T> operation) {
        return operation.contextWrite(ctx -> ctx.put(TOKEN_KEY, token));
    }
}
