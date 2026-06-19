package com.aims.config;

import com.aims.security.JwtTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(h -> h
                .frameOptions(fo -> fo.deny())
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; script-src 'self' https://www.paypal.com https://*.paypal.com https://www.paypalobjects.com https://*.paypalobjects.com; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                    "img-src 'self' data: blob: https:; font-src 'self' data: https://fonts.gstatic.com; " +
                    "connect-src 'self' https:; frame-src https://www.paypal.com https://*.paypal.com; frame-ancestors 'none'; " +
                    "object-src 'none'; base-uri 'self'; form-action 'self'"))
                .referrerPolicy(rp -> rp.policy(
                    ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .permissionsPolicy(pp -> pp.policy("geolocation=(), microphone=(), camera=()"))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/products").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/products/search").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/products/price-histogram").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/products/price-range").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/products/stats").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/products/categories").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/products/*/similar").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/products/*").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/products/stock-batch").permitAll()
                .requestMatchers(HttpMethod.POST,   "/api/products").hasAnyRole("PRODUCT_MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/products/**").hasAnyRole("PRODUCT_MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/products").hasAnyRole("PRODUCT_MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.PATCH,  "/api/products/**").hasAnyRole("PRODUCT_MANAGER", "ADMIN")
                .requestMatchers("/api/manager/**").hasAnyRole("PRODUCT_MANAGER", "ADMIN")
                .requestMatchers("/api/stock-history/**").hasAnyRole("PRODUCT_MANAGER", "ADMIN")
                .requestMatchers("/api/media/**").hasAnyRole("PRODUCT_MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/orders").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/orders/by-email").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/orders/code/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/orders/*/cancel").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/orders/pending").hasAnyRole("PRODUCT_MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.GET,  "/api/orders").hasAnyRole("PRODUCT_MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/orders/*/approve").hasAnyRole("PRODUCT_MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/orders/*/reject").hasAnyRole("PRODUCT_MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.GET,  "/api/orders/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/shipping/calculate").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/paypal/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/vqr/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/vqr/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/contact").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/newsletter/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/config").permitAll()
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(frontendUrl));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        config.setExposedHeaders(List.of("Content-Disposition"));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
