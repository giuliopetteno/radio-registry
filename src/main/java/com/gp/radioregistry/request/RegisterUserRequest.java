package com.gp.radioregistry.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.gp.radioregistry.constant.AppConstants.Validation.*;

public record RegisterUserRequest(
        @Schema(description = "Unique username for the account")
        @NotBlank(message = "Username is required")
        @Size(min=USERNAME_MIN_LENGTH, max=USERNAME_MAX_LENGTH, message = "Username must be between {min} and {max} characters")
        String username,

        @Schema(description = "Unique email address for the account")
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        @Size(max = EMAIL_MAX_LENGTH, message = "Email must not exceed {max} characters")
        String email,

        @Schema(description = "Account password (min 8 characters)")
        @NotBlank(message = "Password is required")
        @Size(min=PASSWORD_MIN_LENGTH, max=PASSWORD_MAX_LENGTH, message = "Password must be between {min} and {max} characters")
        String password
) {}
