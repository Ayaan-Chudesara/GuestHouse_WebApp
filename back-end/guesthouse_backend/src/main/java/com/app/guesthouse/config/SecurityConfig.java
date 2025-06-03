package com.app.guesthouse.config;

import com.app.guesthouse.Service.UserDetailService; // Using your custom UserDetailService
import com.app.guesthouse.security.JwtAuthenticationFilter; // Using your JWT filter
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer; // Ensure this is imported
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
// Removed unused Cors imports as you commented out the bean, but keep if you plan to use it

import java.util.List; // Keep if CorsConfigurationSource is uncommented or for other uses

@EnableMethodSecurity // Enables @PreAuthorize, @PostAuthorize etc.
@EnableWebSecurity // Enables Spring Security's web security support.
@Configuration // Marks this as a Spring configuration class.
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailService userDetailsService; // Your custom UserDetailService

    // Constructor injection for dependencies
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserDetailService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Disable CSRF for stateless REST APIs (since you're using JWTs)
                .csrf(csrf -> csrf.disable())
                // Configure authorization for HTTP requests
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Allow /api/auth/** endpoints (like /login, /register) for everyone
                        .requestMatchers("/api/auth/**").permitAll()
                        // Allow user verification endpoint - using ant pattern
                        .requestMatchers(HttpMethod.GET, "/api/users/*/verify").permitAll()
                        // Admin endpoints require ADMIN role
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // User endpoints require authentication
                        .requestMatchers("/api/bookings/**").authenticated()
                        .requestMatchers("/api/rooms/**").authenticated()
                        .requestMatchers("/api/documents/**").permitAll()
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                // Configure session management to be stateless (no HttpSession)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Configure the UserDetailsService for authentication
                .userDetailsService(userDetailsService)
                // Add your custom JWT authentication filter before the default UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build(); // Build the SecurityFilterChain
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        // This bean is automatically provided by Spring Boot 2.x+ from AuthenticationConfiguration
        // It manages the authentication process, delegating to UserDetailsService and PasswordEncoder.
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Defines the password encoder used for hashing passwords
        return new BCryptPasswordEncoder(); // Ensure this matches what you use in UserService
    }

    @Bean // This bean tells Spring Security to completely ignore certain paths from the security filter chain
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                HttpMethod.OPTIONS, "/**", // Essential: Allows CORS pre-flight requests to pass without authentication
                // List static resources and API documentation paths to be ignored by security
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs/**", // For OpenAPI 3 specification
                "/swagger-resources/**",
                "/swagger-resources/configuration/ui",
                "/swagger-resources/configuration/security",
                "/webjars/**"
                // Do NOT add /api/auth/** here. They need to be within the security filter chain
                // to participate in authentication logic and potentially subsequent filters.
        );
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}