package com.gp.radioregistry.audit.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuditEntityType {
	DEPARTMENT,
	DEVICE,
	DEVICE_TYPE,
	ORGANIZATION,
	ROLE,
	USER
}
