package com.example.collabboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the security filter chain for the application.
     * @param http The HttpSecurity to configure.
     * @return The configured SecurityFilterChain.
     * @throws Exception if an error occurs.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Disable CSRF Protection: This is a common practice for non-browser clients (like your JavaFX app)
            // as they are not vulnerable to cross-site request forgery in the same way browsers are.
            .csrf(AbstractHttpConfigurer::disable)

            // 2. Configure Authorization Rules: Define which endpoints are public and which are protected.
            .authorizeHttpRequests(auth -> auth
                // 3. Permit WebSocket Handshake: This is the critical rule. It tells Spring Security to allow
                // all HTTP requests to any URL starting with "/ws/". This is required for the initial
                // WebSocket connection handshake to succeed without needing authentication.
                .requestMatchers("/ws/**").permitAll()

                // 4. Secure Everything Else: This is a good security practice. It ensures that any other
                // endpoint you might add in the future (e.g., a REST API for user profiles) is protected by default.
                .anyRequest().authenticated()
            );

        // For a desktop app, we don't need formLogin() or httpBasic() if all interaction is through the app.
        // The security context is managed within the stateful application itself.

        return http.build();
    }
}

