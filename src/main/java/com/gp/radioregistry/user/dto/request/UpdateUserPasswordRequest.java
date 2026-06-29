package com.gp.radioregistry.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.gp.radioregistry.constant.ValidationConstants.PASSWORD_MAX_LENGTH;
import static com.gp.radioregistry.constant.ValidationConstants.PASSWORD_MIN_LENGTH;

public record UpdateUserPasswordRequest(
    @Schema(description = "Account password (min 8 characters)")
    @NotBlank(message = "Password is required")
    @Size(min=PASSWORD_MIN_LENGTH, max=PASSWORD_MAX_LENGTH, message = "Password must be between {min} and {max} characters")
    String password
) {}
