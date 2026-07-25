package com.unrn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
                .withJwkSetUri("http://keycloak:8080/realms/cinecloud/protocol/openid-connect/certs").build();

        OAuth2TokenValidator<Jwt> defaultValidators = JwtValidators.createDefault();
        OAuth2TokenValidator<Jwt> issuerValidator = new OAuth2TokenValidator<Jwt>() {
            @Override
            public OAuth2TokenValidatorResult validate(Jwt jwt) {
                String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : "";
                if (issuer.equals("http://localhost:9090/realms/cinecloud") ||
                        issuer.equals("http://keycloak:8080/realms/cinecloud")) {
                    return OAuth2TokenValidatorResult.success();
                }
                return OAuth2TokenValidatorResult
                        .failure(new OAuth2Error("invalid_issuer", "The issuer is not authorized: " + issuer, null));
            }
        };

        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(defaultValidators, issuerValidator));
        return jwtDecoder;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
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
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder())))
                .sessionManagement(session -> session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
