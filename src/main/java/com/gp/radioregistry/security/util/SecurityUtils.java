package com.gp.radioregistry.security.util;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static com.gp.radioregistry.security.constant.SecurityConstants.CORS_MAX_AGE;

public final class SecurityUtils {
	private SecurityUtils() {}

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
