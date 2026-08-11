package com.gp.radioregistry.security.jwt.refreshtoken.service;

import com.gp.radioregistry.security.exception.InvalidRefreshTokenException;
import com.gp.radioregistry.security.jwt.refreshtoken.domain.RefreshToken;
import com.gp.radioregistry.security.jwt.refreshtoken.repository.RefreshTokenRepository;
import com.gp.radioregistry.user.domain.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

import static com.gp.radioregistry.security.jwt.util.JwtUtils.generateSecureRandomToken;
import static com.gp.radioregistry.security.jwt.util.JwtUtils.hashToken;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
	private final RefreshTokenRepository refreshTokenRepository;

	@Getter
	@Value("${jwt.refresh-token.expiration-seconds}")
	private long refreshTokenExpirationSeconds;

	public void saveRefreshToken(RefreshToken refreshToken) {
		refreshTokenRepository.save(refreshToken);
	}

	public String generateAndSaveRefreshToken(User user) {
		var refreshToken = generateSecureRandomToken();
		var tokenHash = hashToken(refreshToken);

		var refreshTokenEntity = RefreshToken.builder()
			.tokenHash(tokenHash)
			.user(user)
			.expiresAt(OffsetDateTime.now().plusSeconds(refreshTokenExpirationSeconds))
			.build();

		saveRefreshToken(refreshTokenEntity);
		return refreshToken;
	}

	public RefreshToken findByTokenHash(String tokenHash) {
		return refreshTokenRepository.findByTokenHash(tokenHash)
			.orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not found"));
	}

	public void validateRefreshToken(RefreshToken refreshToken) {
		if (refreshToken.isRevoked()) {
			refreshTokenRepository.revokeAllByUserId(refreshToken.getUser().getId(), OffsetDateTime.now());
			throw new InvalidRefreshTokenException("Refresh token reuse detected, all sessions revoked");
		}

		if (refreshToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
			throw new InvalidRefreshTokenException("Refresh token expired");
		}
	}
}
