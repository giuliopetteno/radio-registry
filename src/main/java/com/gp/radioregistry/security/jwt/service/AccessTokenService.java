package com.gp.radioregistry.security.jwt.service;

import com.gp.radioregistry.role.domain.Role;
import com.gp.radioregistry.user.domain.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static com.gp.radioregistry.security.constant.SecurityConstants.JWT_ISSUER;
import static com.gp.radioregistry.security.constant.SecurityConstants.JWT_ROLES_CLAIM;

@Service
@RequiredArgsConstructor
public class AccessTokenService {
	private final JwtEncoder jwtEncoder;

	@Getter
	@Value("${jwt.access-token.expiration-seconds}")
	private long accessTokenExpirationSeconds;

	public String generateAccessToken(User user) {
		var now = Instant.now();
		var roles = user.getRoles().stream()
								.map(Role::getName)
								.toList();

		var jwtClaimsSet = JwtClaimsSet.builder()
			.issuer(JWT_ISSUER)
			.issuedAt(now)
			.expiresAt(now.plusSeconds(accessTokenExpirationSeconds))
			.subject(user.getUsername())
			.claim(JWT_ROLES_CLAIM, roles)
			.build();

		return jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSet)).getTokenValue();
	}
}