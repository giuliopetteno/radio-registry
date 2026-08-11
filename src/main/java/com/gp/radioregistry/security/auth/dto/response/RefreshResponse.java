package com.gp.radioregistry.security.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record RefreshResponse(
	@Schema(description = "Newly issued access token")
	String accessToken,

	@Schema(description = "Newly issued refresh token")
	String refreshToken,

	@Schema(description = "Access token validity in seconds")
	long expiresIn,

	@Schema(description = "Timestamp of the refresh operation")
	Instant refreshTime
) {}