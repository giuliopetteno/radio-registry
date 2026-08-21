package com.gp.radioregistry.security.jwt.refreshtoken.repository;

import com.gp.radioregistry.security.jwt.refreshtoken.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByTokenHash(String tokenHash);

	Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);

	@Modifying
	@Query("UPDATE RefreshToken t SET t.revoked = true, t.revokedAt = :revokedAt WHERE t.id = :id AND t.revoked = false")
	int revokeIfActive(@Param("id") Long id, @Param("revokedAt") OffsetDateTime revokedAt);

	@Modifying
	@Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = :revokedAt WHERE rt.user.id = :userId AND rt.revoked = false")
	void revokeAllByUserId(@Param("userId") Long userId, @Param("revokedAt") OffsetDateTime revokedAt);

	@Modifying
	@Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :deleteBefore OR (rt.revoked = true AND rt.revokedAt < :deleteBefore)")
	int deleteExpiredOrRevokedBefore(@Param("deleteBefore") OffsetDateTime deleteBefore);
}