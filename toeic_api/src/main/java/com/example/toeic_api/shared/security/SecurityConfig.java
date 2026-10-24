package com.example.toeic_api.shared.security;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {
    private final String[] PUBLIC_ENDPOINTS = {
        "/api/v1/users",
        "/api/v1/auth/login",
        "/api/v1/auth/introspect",
        "/api/v1/auth/refresh",
        "/api/v1/auth/logout",
        "/api/v1/auth/forgot-password",
        "/api/v1/auth/reset-password",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/swagger-ui.html"
    };

    @Autowired private CustomJwtDecoder customJwtDecoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.sessionManagement(
                session ->
                        session.sessionCreationPolicy(
                                org.springframework.security.config.http.SessionCreationPolicy
                                        .STATELESS));
        httpSecurity.cors(org.springframework.security.config.Customizer.withDefaults());
        httpSecurity.requestCache(cache -> cache.disable());
        httpSecurity.authorizeHttpRequests(
                request ->
                        request.requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS)
                                .permitAll()
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/swagger-ui.html")
                                .permitAll()
                                .requestMatchers("/api/v1/admin/**")
                                .hasRole("ADMIN")
                                .anyRequest()
                                .authenticated());

        httpSecurity.oauth2ResourceServer(
                oauth2 ->
                        oauth2.jwt(
                                        jwtConfigurer ->
                                                jwtConfigurer
                                                        .decoder(customJwtDecoder)
                                                        .jwtAuthenticationConverter(
                                                                jwtAuthenticationConverter()))
                                .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));

        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        return httpSecurity.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource(
            @org.springframework.beans.factory.annotation.Value(
                            "${app.password-reset.frontend-url:http://localhost:5173}")
                    String frontend) {
        var cors = new org.springframework.web.cors.CorsConfiguration();
        cors.setAllowedOrigins(java.util.List.of(frontend.replaceAll("/$", "")));
        cors.setAllowedMethods(
                java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type"));
        var source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
                new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("scope");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
                jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
