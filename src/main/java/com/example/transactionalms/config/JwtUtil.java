package com.example.transactionalms.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
    private static String secretKey = "s3r610";
    private static Algorithm algorithm = Algorithm.HMAC256(secretKey);

    public String getUserName(String jwt) {
        return JWT.require(algorithm)
                .build()
                .verify(jwt)
                .getSubject();
    }

    public boolean isValid(String jwt) {
        try {
            JWT.require(algorithm)
                    .build()
                    .verify(jwt);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }
}
