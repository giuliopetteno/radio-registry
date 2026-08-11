package com.gp.radioregistry.security.util;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

import static com.gp.radioregistry.security.constant.SecurityConstants.*;

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

	public static String generateSecureRandomToken() {
		byte[] bytes = new byte[REFRESH_TOKEN_BYTE_LENGTH];
		new SecureRandom().nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	public static String hashToken(String token) {
		try {
			var messageDigest = MessageDigest.getInstance(REFRESH_TOKEN_HASH_ALGORITHM);
			byte[] hashBytes = messageDigest.digest(token.getBytes());
			return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(REFRESH_TOKEN_HASH_ALGORITHM + " algorithm not available", e);
		}
	}
}
