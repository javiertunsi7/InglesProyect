package com.englishlearning.security;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .anonymous(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/exercises/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/dashboard").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/me").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/words/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/dictionary").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/questions/*/answer").permitAll()
                        .requestMatchers("/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/v1/push/vapid-public-key").permitAll()
                        .requestMatchers("/v1/push/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/v1/stripe/webhook").permitAll()
                        .requestMatchers("/v1/stripe/**").authenticated()
                        .requestMatchers("/v1/subscription/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/v1/leaderboard").authenticated()
                        .requestMatchers(HttpMethod.GET, "/v1/practice/**").authenticated()
                        .requestMatchers("/v1/quests/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/v1/progress").authenticated()
                        .requestMatchers("/v1/progress/**").authenticated()
                        .requestMatchers("/v1/users/me/**").authenticated()
                        .requestMatchers("/h2-console/**").permitAll()
                        .anyRequest().authenticated())
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, e) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"message\":\"Tu sesión ha expirado o no has iniciado sesión.\"}");
                        })
                        .accessDeniedHandler((request, response, e) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"message\":\"No tienes permisos para realizar esta acción.\"}");
                        }))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
