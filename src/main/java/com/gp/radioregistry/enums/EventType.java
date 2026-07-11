package com.gp.radioregistry.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EventType {
	CREATE,
	READ,
	UPDATE,
	DELETE,
	LOGIN,
	LOGOUT
}
