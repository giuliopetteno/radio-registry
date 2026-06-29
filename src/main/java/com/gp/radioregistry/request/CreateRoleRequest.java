package com.gp.radioregistry.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.gp.radioregistry.constant.AppConstants.Validation.NAME_MAX_LENGTH;

public record CreateRoleRequest(
    @Schema(description = "Unique role name")
    @NotBlank(message = "Role name is required")
    @Size(max = NAME_MAX_LENGTH, message = "Role name must not exceed {max} characters")
    String name

) {}
