package com.gp.radioregistry.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UpdateUserRolesRequest(
    @Schema(description = "Account roles")
    @NotEmpty(message = "At least one role must be provided")
	Set<String> roleNames
) {}
