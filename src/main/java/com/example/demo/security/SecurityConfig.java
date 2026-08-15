package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                // GitHub webhook uses HMAC authentication
                .requestMatchers("/api/webhooks/github")
                .permitAll()

                // Other endpoints require authentication
                .anyRequest()
                .authenticated()
            );

        return http.build();
    }
}