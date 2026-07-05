package com.gp.radioregistry.audit.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuditAction {
	CREATE,
	READ,
	UPDATE,
	DELETE,
	LOGIN,
	LOGOUT
}
