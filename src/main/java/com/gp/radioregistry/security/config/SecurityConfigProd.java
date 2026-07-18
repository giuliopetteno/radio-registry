package com.gp.radioregistry.security.config;

import com.gp.radioregistry.security.enums.Role;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static com.gp.radioregistry.constant.ApiConstants.*;
import static com.gp.radioregistry.constant.SecurityConstants.SESSION_COOKIE;
import static com.gp.radioregistry.constant.SecurityConstants.SESSION_TIMEOUT_SEC;
import static org.springframework.security.config.Customizer.withDefaults;

@Slf4j
@Configuration
@Profile("prod")
public class SecurityConfigProd {
   @Bean
   SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
            // CSRF protection is disabled because JWT authentication does not need cookies
            .csrf(CsrfConfigurer::disable)
            // Using HTTPS protocol only for production environment
            .redirectToHttps(https -> https.requestMatchers(AnyRequestMatcher.INSTANCE))
            .authorizeHttpRequests(requests -> requests
                .requestMatchers(AUTH_PATH+WC_ALL, SWAGGER_PATH+WC_ALL, V3_API_DOCS_PATH+WC_ALL).permitAll()
                .requestMatchers(HttpMethod.GET, ORGANIZATIONS_PATH+WC_ALL, DEPARTMENTS_PATH+WC_ALL,
                    DEVICES_PATH+WC_ALL, DEVICE_TYPES_PATH+WC_ALL)
                    .hasAnyRole(Role.OPERATOR.getName(), Role.TECHNICIAN.getName(), Role.ADMIN.getName())
                .requestMatchers(ORGANIZATIONS_PATH+WC_ALL, DEPARTMENTS_PATH+WC_ALL, DEVICES_PATH+WC_ALL,
                    DEVICE_TYPES_PATH+WC_ALL).hasAnyRole(Role.TECHNICIAN.getName(), Role.ADMIN.getName())
                .anyRequest().hasRole(Role.ADMIN.getName()))
            .formLogin(withDefaults())
            .httpBasic(withDefaults()).sessionManagement(session -> session
                // Temporary setup before migrating to JWT auth: maintaining session state via JSESSIONID to avoid re-sending Basic auth credentials
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .logout(logout -> logout
                .logoutUrl(AUTH_PATH+LOGOUT_PATH)
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

    // CORS is explicitly restricted to localhost.
    // Allowed origins must be configured for the trusted frontend.
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(List.of(String.format("%s://%s:%s", PROTOCOL_HTTPS, DOMAIN, PORT)));
        corsConfig.setAllowedMethods(List.of("*"));
        corsConfig.setAllowCredentials(true);
        corsConfig.setAllowedHeaders(List.of("*"));
        corsConfig.setMaxAge(SESSION_TIMEOUT_SEC);

        UrlBasedCorsConfigurationSource corsConfigSource = new UrlBasedCorsConfigurationSource();
        corsConfigSource.registerCorsConfiguration("/**", corsConfig);
        return corsConfigSource;
    }
}
