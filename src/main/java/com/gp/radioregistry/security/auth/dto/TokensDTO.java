package com.gp.radioregistry.security.auth.dto;

import com.gp.radioregistry.user.domain.User;

public record TokensDTO(
	String accessToken,

	String refreshToken,

	long expiresIn,

	User user
) {}
