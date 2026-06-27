package com.gp.radioregistry.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @Schema(description = "Unique username for the account")
        @NotBlank(message = "Username is required")
        @Size(min=3, max=50, message = "Username must be between 3 and 50 characters")
        String username,

        @Schema(description = "Unique email address for the account")
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        @Size(max = 50, message = "Email must not exceed 50 characters")
        String email,

        @Schema(description = "Account password (min 8 characters)")
        @NotBlank(message = "Password is required")
        @Size(min=8, max=200, message = "Password must be at least 8 characters")
        String password
) {}
