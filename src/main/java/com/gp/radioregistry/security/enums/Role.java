package com.gp.radioregistry.security.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
	ADMIN(1L, "ADMIN"),
	TECHNICIAN(2L,"TECHNICIAN"),
	OPERATOR(3L,"OPERATOR");

	private final Long id;
	private final String name;
}
