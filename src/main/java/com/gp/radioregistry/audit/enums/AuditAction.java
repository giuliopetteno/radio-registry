package com.gp.radioregistry.audit.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuditAction {
	CREATE("CREATE"),
	READ("READ"),
	UPDATE("UPDATE"),
	DELETE("DELETE"),
	LOGIN("LOGIN"),
	LOGOUT("LOGOUT");

	private final String text;
}
