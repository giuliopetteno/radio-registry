package com.gp.radioregistry.security.jwt.refreshtoken.scheduler;

import com.gp.radioregistry.security.jwt.refreshtoken.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanup {
	private final RefreshTokenRepository refreshTokenRepository;

	@Value("${jwt.refreshtoken.cleanup.retention.days}")
	private int retentionDays;

	@Transactional
	@Scheduled(cron = "${jwt.refreshtoken.cleanup.cron}")
	public void cleanupExpiredAndRevokedTokens() {
		var deleteBefore = OffsetDateTime.now().minusDays(retentionDays);
		var deletedCount = refreshTokenRepository.deleteExpiredOrRevokedBefore(deleteBefore);

		log.info("Refresh token cleanup completed, {} tokens deleted", deletedCount);
	}
}
