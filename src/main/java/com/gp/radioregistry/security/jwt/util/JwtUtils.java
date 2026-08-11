package com.gp.radioregistry.security.jwt.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import static com.gp.radioregistry.security.jwt.constant.JwtConstants.REFRESH_TOKEN_BYTE_LENGTH;
import static com.gp.radioregistry.security.jwt.constant.JwtConstants.REFRESH_TOKEN_HASH_ALGORITHM;

public final class JwtUtils {
	private JwtUtils() {}

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
