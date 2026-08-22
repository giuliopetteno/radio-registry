package com.gp.radioregistry.security.jwt.refreshtoken.service;

import com.gp.radioregistry.security.exception.InvalidRefreshTokenException;
import com.gp.radioregistry.security.jwt.refreshtoken.domain.RefreshToken;
import com.gp.radioregistry.security.jwt.refreshtoken.repository.RefreshTokenRepository;
import com.gp.radioregistry.user.domain.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

	public String generateAndSaveRefreshToken(User user, RefreshToken oldRefreshTokenEntity) {
		var refreshToken = generateSecureRandomToken();
		var tokenHash = hashToken(refreshToken);

		var refreshTokenEntity = RefreshToken.builder()
			.tokenHash(tokenHash)
			.user(user)
			.expiresAt(OffsetDateTime.now().plusSeconds(refreshTokenExpirationSeconds))
			.build();

		saveRefreshToken(refreshTokenEntity);

		if (oldRefreshTokenEntity != null) {
			oldRefreshTokenEntity.setReplacedByToken(refreshTokenEntity);
			saveRefreshToken(oldRefreshTokenEntity);
		}

		return refreshToken;
	}

	public int revokeIfActive(Long refreshTokenId, OffsetDateTime revokedAt) {
		return refreshTokenRepository.revokeIfActive(refreshTokenId, revokedAt);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void revokeAllByUserId(Long userId, OffsetDateTime revokedAt) {
		refreshTokenRepository.revokeAllByUserId(userId, revokedAt);
	}

	public RefreshToken findByTokenHash(String tokenHash) {
		return refreshTokenRepository.findByTokenHash(tokenHash)
			.orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not found"));
	}
}
