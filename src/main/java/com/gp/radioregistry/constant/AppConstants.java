package com.gp.radioregistry.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

public final class AppConstants {

	private AppConstants() {
	}

	// ── API ──────────────────────────────────────────────────────────────────
	public static final class Api {
		private Api() {}

		public static final String PROTOCOL_HTTP    		= "http";
		public static final String PROTOCOL_HTTPS   		= "https";
		public static final String DOMAIN  					= "localhost";
		public static final String PORT 					= "4200";
		public static final String LOGOUT_PATH 				= "/logout";
		public static final String AUTH_PATH 				= "/auth";
		public static final String USERS_PATH 				= "/users";
		public static final String ROLES_PATH 				= "/roles";
		public static final String DEVICES_PATH 			= "/devices";
		public static final String DEVICE_TYPES_PATH 		= "/devices-types";
		public static final String COMPARTMENTS_PATH 		= "/compartments";
		public static final String ORGANIZATIONS_PATH 		= "/organizations";
	}

	// ── Security ─────────────────────────────────────────────────────────────
	public static final class Security {
		private Security() {}

		@Getter
		@AllArgsConstructor
		public enum Role {
			ADMIN(1L, "ADMIN"),
			TECHNICIAN(2L,"TECHNICIAN"),
			OPERATOR(3L,"OPERATOR");

			private final Long id;
			private final String name;
		}

		public static final int    BCRYPT_STRENGTH      = 12;
		public static final String SESSION_COOKIE		= "JSESSIONID";
		public static final long   SESSION_TIMEOUT_SEC  = 3600L;
		public static final String ROLE_PREFIX 			= "ROLE_";
	}

	// ── Pagination ───────────────────────────────────────────────────────────
	public static final class Pagination {
		private Pagination() {}

		public static final int DEFAULT_PAGE      = 0;
		public static final int DEFAULT_PAGE_SIZE = 20;
		public static final int MAX_PAGE_SIZE     = 100;
	}

	// ── Validation ───────────────────────────────────────────────────────────
	public static final class Validation {
		private Validation() {}

		public static final int USERNAME_MIN_LENGTH  	 = 3;
		public static final int USERNAME_MAX_LENGTH  	 = 50;
		public static final int EMAIL_MAX_LENGTH      	 = 50;
		public static final int PASSWORD_MIN_LENGTH  	 = 8;
		public static final int PASSWORD_MAX_LENGTH  	 = 30;
		public static final int NAME_MAX_LENGTH      	 = 50;
		public static final int CODE_MAX_LENGTH      	 = 20;
		public static final int DESCRIPTION_MAX_LENGTH 	 = 200;
		public static final int SERIAL_NUMBER_MAX_LENGTH = 20;
	}

	// ── Date / Time ──────────────────────────────────────────────────────────
	public static final class DateTime {
		private DateTime() {}

		public static final String DEFAULT_DATE_FORMAT      = "yyyy-MM-dd";
		public static final String DEFAULT_DATETIME_FORMAT  = "yyyy-MM-dd'T'HH:mm:ss";
		public static final String DEFAULT_TIMEZONE         = "UTC";
	}

	// ── Error messages ───────────────────────────────────────────────────────
	public static final class ErrorMessage {
		private ErrorMessage() {}
	}
}
