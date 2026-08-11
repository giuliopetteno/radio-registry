package com.gp.radioregistry.security.auth.controller;

import com.gp.radioregistry.security.auth.dto.request.LoginRequest;
import com.gp.radioregistry.security.auth.dto.request.LogoutRequest;
import com.gp.radioregistry.security.auth.dto.request.RefreshRequest;
import com.gp.radioregistry.security.auth.dto.request.RegisterUserRequest;
import com.gp.radioregistry.security.auth.dto.response.AuthResponse;
import com.gp.radioregistry.security.auth.dto.response.RefreshResponse;
import com.gp.radioregistry.security.auth.service.AuthenticationService;
import com.gp.radioregistry.user.dto.response.UserResponse;
import com.gp.radioregistry.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Instant;

import static com.gp.radioregistry.constant.ApiConstants.AUTH_PATH;
import static com.gp.radioregistry.constant.ApiConstants.USERS_PATH;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(AUTH_PATH)
@Tag(name = "Auth controller", description = "API for authentication and authorization")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Receives a new user, validates it and register it.")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterUserRequest request) {
        log.info("Creation request received for user with username: {}", request.username());
        var user = userService.createUser(request);

        return ResponseEntity.created(URI.create(String.format("%s/%d", USERS_PATH, user.getId())))
            .body(UserResponse.fromEntity(user));
    }

    @PostMapping("/login")
    @Operation(summary = "Performs login", description = "Authenticates a user and returns a response with user details.")
    public ResponseEntity<AuthResponse> doLogin(@Valid @RequestBody LoginRequest request) {
        var authentication = authenticationService.doAuthentication(request);
        var tokensDTO = authenticationService.generateTokens(authentication);

        return ResponseEntity.ok(new AuthResponse(
            UserResponse.fromEntity(tokensDTO.user()),
            Instant.now(),
            tokensDTO.accessToken(),
            tokensDTO.refreshToken(),
            tokensDTO.expiresIn()
        ));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Exchanges a valid refresh token for a new access/refresh token pair.")
    public ResponseEntity<RefreshResponse> doRefresh(@Valid @RequestBody RefreshRequest request) {
        var tokensDTO = authenticationService.refreshTokens(request.refreshToken());

        log.info("Access token refreshed successfully for user {}", tokensDTO.user().getUsername());
        return ResponseEntity.ok(new RefreshResponse(
            tokensDTO.accessToken(),
            tokensDTO.refreshToken(),
            tokensDTO.expiresIn(),
            Instant.now()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Performs logout", description = "Revokes the given refresh token.")
    public ResponseEntity<Void> doLogout(@Valid @RequestBody LogoutRequest request) {
        var username = authenticationService.logout(request.refreshToken());

        log.info("Logout successful for the user {}", username);
        return ResponseEntity.noContent().build();
    }
}
