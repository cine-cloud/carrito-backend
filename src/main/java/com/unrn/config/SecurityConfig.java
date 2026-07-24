package com.unrn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;

@Configuration
public class SecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http)
                        throws Exception {

                http
                                .csrf(csrf -> csrf.disable())
                                .cors(cors -> {
                                })
                                .authorizeHttpRequests(auth -> auth

                                                .requestMatchers(HttpMethod.OPTIONS, "/**")
                                                .permitAll()

                                                // Flujo anónimo
                                                .requestMatchers(HttpMethod.POST, "/carritos")
                                                .permitAll()

                                                .requestMatchers(HttpMethod.GET, "/carritos/*")
                                                .permitAll()

                                                .requestMatchers(HttpMethod.POST, "/carritos/agregar-item/*")
                                                .permitAll()

                                                .requestMatchers(HttpMethod.PUT, "/carritos/actualizar-cantidad/*")
                                                .permitAll()

                                                .requestMatchers(HttpMethod.DELETE, "/carritos/eliminar-item/*")
                                                .permitAll()

                                                // Usuario autenticado
                                                .requestMatchers(HttpMethod.GET, "/carritos/usuario/*")
                                                .authenticated()

                                                .requestMatchers(HttpMethod.PUT, "/carritos/*/fusionar/*")
                                                .authenticated()

                                                .requestMatchers(HttpMethod.POST, "/carritos/checkout/*")
                                                .authenticated()

                                                .anyRequest().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2.jwt())
                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS));

                return http.build();
        }
}
