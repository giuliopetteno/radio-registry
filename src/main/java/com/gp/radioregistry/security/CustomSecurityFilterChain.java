package com.gp.radioregistry.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static com.gp.radioregistry.constant.AppConstants.Api.*;
import static com.gp.radioregistry.constant.AppConstants.Security.*;
import static org.springframework.security.config.Customizer.withDefaults;

@Log4j2
@Configuration
@Profile("!prod")
public class CustomSecurityFilterChain {
   @Bean
   SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
            // CSRF protection is disabled because JWT authentication does not need cookies
            .csrf(CsrfConfigurer::disable)
            // Using HTTP protocol only for non-production environments
            .redirectToHttps(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(requests -> requests
                .requestMatchers("/roles/**", "/organizations/**", "/compartments/**", "/devices/**", "/device-types/**").hasRole(ROLE_USER)
                .requestMatchers("/auth/**").permitAll())
            .formLogin(withDefaults())
            .httpBasic(withDefaults()).sessionManagement(session -> session
                // Temporary setup before migrating to JWT auth: maintaining session state via JSESSIONID to avoid re-sending Basic auth credentials
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .logout(logout -> logout
                .logoutUrl(LOGOUT_API)
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies(SESSION_COOKIE)
                .logoutSuccessHandler((_, response, authentication) -> {
                    log.info("Logout successful for the user {}", authentication != null ? authentication.getName() : "");
                    response.setStatus(HttpServletResponse.SC_OK);
                })
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
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(List.of(PROTOCOL_HTTP+DOMAIN+PORT));
        corsConfig.setAllowedMethods(List.of("*"));
        corsConfig.setAllowCredentials(true);
        corsConfig.setAllowedHeaders(List.of("*"));
        corsConfig.setMaxAge(SESSION_TIMEOUT_SEC);

        UrlBasedCorsConfigurationSource corsConfigSource = new UrlBasedCorsConfigurationSource();
        corsConfigSource.registerCorsConfiguration("/**", corsConfig);
        return corsConfigSource;
    }
}
