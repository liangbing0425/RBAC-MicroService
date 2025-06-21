// JwtConfig.java - JWT配置
package org.shuzimali.userservice.config;

import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.security.Key;

@Configuration
public class JwtConfig {

    private static final String SECRET_KEY = "userservicejwtsecretkey12345678901234567890123456";

    @Bean
    public SecretKey secretKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }
}