package com.gp.radioregistry.audit.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuditEntityType {
	DEPARTMENT("DEPARTMENT"),
	DEVICE("DEVICE"),
	DEVICE_TYPE("DEVICE_TYPE"),
	ORGANIZATION("ORGANIZATION"),
	ROLE("ROLE"),
	USER("USER"),
	USER_ROLES("USER_ROLES"),
	USER_PASSWORD("USER_PASSWORD");

	private final String text;
}
