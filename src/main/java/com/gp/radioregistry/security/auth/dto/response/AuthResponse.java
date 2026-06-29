package com.gp.radioregistry.security.auth.dto.response;

import com.gp.radioregistry.user.dto.response.UserResponse;

import java.time.Instant;

public record AuthResponse(
	UserResponse user,
	Instant loginTime
) {}
