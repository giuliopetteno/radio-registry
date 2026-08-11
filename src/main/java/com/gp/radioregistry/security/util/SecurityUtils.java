package com.gp.radioregistry.security.util;

import com.gp.radioregistry.security.enums.Role;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static com.gp.radioregistry.constant.ApiConstants.*;
import static com.gp.radioregistry.security.constant.SecurityConstants.CORS_MAX_AGE;

public final class SecurityUtils {
	private SecurityUtils() {}

	public static void getAuthorizationRules(
		AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests) {
		requests
			.requestMatchers(AUTH_PATH + "/register", AUTH_PATH + "/login", AUTH_PATH + "/refresh", AUTH_PATH + "/logout",
				"/swagger-ui" + WC_ALL, "/v3/api-docs" + WC_ALL)
				.permitAll()
			.requestMatchers(HttpMethod.GET, ORGANIZATIONS_PATH + WC_ALL, DEPARTMENTS_PATH + WC_ALL, DEVICES_PATH + WC_ALL,
				DEVICE_TYPES_PATH + WC_ALL)
				.hasAnyRole(Role.OPERATOR.getName(), Role.TECHNICIAN.getName(), Role.ADMIN.getName())
			.requestMatchers(ORGANIZATIONS_PATH + WC_ALL, DEPARTMENTS_PATH + WC_ALL, DEVICES_PATH + WC_ALL,
				DEVICE_TYPES_PATH + WC_ALL)
				.hasAnyRole(Role.TECHNICIAN.getName(), Role.ADMIN.getName())
			.anyRequest().hasRole(Role.ADMIN.getName());
	}

	public static CorsConfigurationSource buildCorsConfigurationSource(List<String> allowedOrigins) {
		var corsConfig = new CorsConfiguration();
		corsConfig.setAllowedOrigins(allowedOrigins);
		corsConfig.setAllowedMethods(List.of("*"));
		corsConfig.setAllowCredentials(true);
		corsConfig.setAllowedHeaders(List.of("*"));
		corsConfig.setMaxAge(CORS_MAX_AGE);

		var corsConfigSource = new UrlBasedCorsConfigurationSource();
		corsConfigSource.registerCorsConfiguration("/**", corsConfig);
		return corsConfigSource;
	}
}
