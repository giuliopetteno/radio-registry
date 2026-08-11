package com.gp.radioregistry.security.jwt.constant;

import com.nimbusds.jose.JWSAlgorithm;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

public final class JwtConstants {
	private JwtConstants() {}

	public static final String JWT_ISSUER 						= "radio-registry";
	public static final String JWT_ROLES_CLAIM 					= "roles";
	public static final int REFRESH_TOKEN_BYTE_LENGTH 			= 32;
	public static final MacAlgorithm ACCESS_TOKEN_JWS_ALGORITHM = MacAlgorithm.HS256;
	public static final JWSAlgorithm ACCESS_TOKEN_JWK_ALGORITHM = JWSAlgorithm.HS256;
	public static final String ACCESS_TOKEN_SIGNING_ALGORITHM	= "HmacSHA256";
	public static final String REFRESH_TOKEN_HASH_ALGORITHM 	= "SHA-256";
}
