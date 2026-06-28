package com.gp.radioregistry.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import static com.gp.radioregistry.constant.AppConstants.Validation.*;

public record UpdateUserRequest(
    @Schema(description = "Account username")
    @Size(min=USERNAME_MIN_LENGTH, max=USERNAME_MAX_LENGTH, message = "Username must be between {min} and {max} characters")
    String username,

    @Schema(description = "Account email address")
    @Email(message = "Email must be a valid email address")
    @Size(max = EMAIL_MAX_LENGTH, message = "Email must not exceed {max} characters")
    String email
) {
    @AssertTrue(message = "Either username or email must be specified")
    public boolean userOrEmailValid() {
    return (username != null && !username.isBlank()) != (email != null && !email.isBlank());
  }
}
