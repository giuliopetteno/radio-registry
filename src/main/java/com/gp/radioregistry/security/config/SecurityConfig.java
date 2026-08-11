package com.gp.radioregistry.security.config;

import com.gp.radioregistry.security.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static com.gp.radioregistry.constant.ApiConstants.*;
import static com.gp.radioregistry.security.util.SecurityUtils.buildCorsConfigurationSource;

@Slf4j
@Configuration
@Profile("!prod")
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

   @Bean
   SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
            // CSRF protection is disabled because JWT authentication does not need cookies
            .csrf(CsrfConfigurer::disable)
            // Using HTTP protocol only for non-production environments
            .redirectToHttps(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(requests -> requests
                    .requestMatchers(AUTH_PATH + "/register", AUTH_PATH + "/login", AUTH_PATH + "/refresh", AUTH_PATH + "/logout",
                        "/swagger-ui" + WC_ALL, "/v3/api-docs" + WC_ALL).permitAll()
                    .requestMatchers(HttpMethod.GET, ORGANIZATIONS_PATH + WC_ALL, DEPARTMENTS_PATH + WC_ALL, DEVICES_PATH + WC_ALL,
                        DEVICE_TYPES_PATH + WC_ALL).hasAnyRole(Role.OPERATOR.getName(), Role.TECHNICIAN.getName(), Role.ADMIN.getName())
                    .requestMatchers(ORGANIZATIONS_PATH + WC_ALL, DEPARTMENTS_PATH + WC_ALL, DEVICES_PATH + WC_ALL,
                        DEVICE_TYPES_PATH + WC_ALL).hasAnyRole(Role.TECHNICIAN.getName(), Role.ADMIN.getName())
                    .anyRequest().hasRole(Role.ADMIN.getName()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
            );
        return http.build();
    }

    /*  Disabled, using CustomUserDetailsService
    @Bean
    public UserDetailsService userDetailsService(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }*/

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public CompromisedPasswordChecker compromisedPasswordChecker() {
        return new HaveIBeenPwnedRestApiPasswordChecker();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // CORS is explicitly restricted to localhost for local development.
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        return buildCorsConfigurationSource(List.of(String.format("%s://%s:%s", PROTOCOL_HTTP, DOMAIN, PORT)));
    }
}
