package com.gp.radioregistry.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EventType {
	READ,
	CREATE,
	UPDATE,
	STATUS_CHANGED,
	DELETE,
	LOGIN,
	LOGOUT
}
