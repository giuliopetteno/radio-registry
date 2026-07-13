package com.gp.radioregistry.constant;

public final class SecurityConstants {
	private SecurityConstants() {}

	public static final int    BCRYPT_STRENGTH      = 12;
	public static final String SESSION_COOKIE		= "JSESSIONID";
	public static final long   SESSION_TIMEOUT_SEC  = 3600L;
	public static final String ROLE_PREFIX 			= "ROLE_";
}
