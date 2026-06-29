package com.gp.radioregistry.security.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "Account username")
        @NotBlank(message = "Username is required")
        String username,

        @Schema(description = "Account password")
        @NotBlank(message = "Password is required")
        String password
) {}
