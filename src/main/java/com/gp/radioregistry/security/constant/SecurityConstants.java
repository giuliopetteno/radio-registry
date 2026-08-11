package com.gp.radioregistry.security.constant;

public final class SecurityConstants {
	private SecurityConstants() {}

	public static final int    BCRYPT_STRENGTH      			= 12;
	public static final String SESSION_COOKIE					= "JSESSIONID";
	public static final long   CORS_MAX_AGE			  			= 3600L;
	public static final String ROLE_PREFIX 						= "ROLE_";
	public static final String JWT_ISSUER 						= "radio-registry";
	public static final String JWT_ROLES_CLAIM 					= "roles";
	public static final int REFRESH_TOKEN_BYTE_LENGTH 			= 32;
	public static final String REFRESH_TOKEN_HASH_ALGORITHM 	= "SHA-256";
	public static final String ACCESS_TOKEN_SIGNING_ALGORITHM	= "HmacSHA256";
}
