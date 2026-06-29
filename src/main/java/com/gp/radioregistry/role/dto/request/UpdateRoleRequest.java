package com.gp.radioregistry.role.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import static com.gp.radioregistry.constant.ValidationConstants.NAME_MAX_LENGTH;

public record UpdateRoleRequest(
	@Schema(description = "Role name - if provided, updates the current value")
	@Size(max = NAME_MAX_LENGTH, message = "Role name must not exceed {max} characters")
	String name
) {}
