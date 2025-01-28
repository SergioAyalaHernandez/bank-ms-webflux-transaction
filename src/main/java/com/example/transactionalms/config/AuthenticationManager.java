package com.example.transactionalms.config;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@Component
public class AuthenticationManager implements ReactiveAuthenticationManager {
    private final JwtUtil jwtUtil;

    public AuthenticationManager(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();

        try {
            if (jwtUtil.isValid(authToken)) {
                String username = jwtUtil.getUserName(authToken);
                return Mono.just(new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        new ArrayList<>()
                ));
            } else {
                return Mono.empty();
            }
        } catch (Exception e) {
            return Mono.empty();
        }
    }
}
