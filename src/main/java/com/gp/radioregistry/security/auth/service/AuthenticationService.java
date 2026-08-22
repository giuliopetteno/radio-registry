package com.gp.radioregistry.security.auth.service;

import com.gp.radioregistry.audit.annotation.Auditable;
import com.gp.radioregistry.enums.EntityType;
import com.gp.radioregistry.enums.EventType;
import com.gp.radioregistry.security.auth.dto.TokensDTO;
import com.gp.radioregistry.security.auth.dto.request.LoginRequest;
import com.gp.radioregistry.security.exception.InvalidRefreshTokenException;
import com.gp.radioregistry.security.jwt.refreshtoken.service.RefreshTokenService;
import com.gp.radioregistry.security.jwt.service.AccessTokenService;
import com.gp.radioregistry.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static com.gp.radioregistry.security.jwt.util.JwtUtils.hashToken;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    @Auditable(eventType = EventType.LOGIN, entityType = EntityType.USER, description = "User login attempt")
    public Authentication doAuthentication(LoginRequest loginRequest) {
        return authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));
    }

    public TokensDTO generateTokens(Authentication authentication) {
        var user = userService.findByUsernameOrEmail(authentication.getName());

        var accessToken = accessTokenService.generateAccessToken(user);
        var refreshToken = refreshTokenService.generateAndSaveRefreshToken(user, null);

        return new TokensDTO(accessToken, refreshToken, accessTokenService.getAccessTokenExpirationSeconds(), user);
    }

    @Transactional
    public TokensDTO refreshTokens(String refreshToken) {
        var now = OffsetDateTime.now();
        var tokenHash = hashToken(refreshToken);
        var refreshTokenEntity = refreshTokenService.findByTokenHash(tokenHash);

        if (refreshTokenEntity.getExpiresAt().isBefore(now)) {
            throw new InvalidRefreshTokenException("Refresh token expired");
        }

        if (refreshTokenService.revokeIfActive(refreshTokenEntity.getId(), now) == 0) {
            refreshTokenService.revokeAllByUserId(refreshTokenEntity.getUser().getId(), now);
            throw new InvalidRefreshTokenException("Refresh token reuse detected, all sessions revoked");
        }

        var user = refreshTokenEntity.getUser();
        var newAccessToken = accessTokenService.generateAccessToken(user);
        var newRefreshToken = refreshTokenService.generateAndSaveRefreshToken(user, refreshTokenEntity);

        return new TokensDTO(newAccessToken, newRefreshToken,
            accessTokenService.getAccessTokenExpirationSeconds(), user);
    }

    public String logout(String refreshToken) {
        var tokenHash = hashToken(refreshToken);
        var refreshTokenEntity = refreshTokenService.findByTokenHash(tokenHash);

        refreshTokenEntity.setRevoked(true);
        refreshTokenEntity.setRevokedAt(OffsetDateTime.now());
        refreshTokenService.saveRefreshToken(refreshTokenEntity);

        return refreshTokenEntity.getUser().getUsername();
    }
}