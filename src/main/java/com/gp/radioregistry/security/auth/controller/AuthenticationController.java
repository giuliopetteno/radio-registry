package com.gp.radioregistry.security.auth.controller;

import com.gp.radioregistry.security.auth.dto.request.LoginRequest;
import com.gp.radioregistry.security.auth.dto.request.RegisterUserRequest;
import com.gp.radioregistry.security.auth.dto.response.AuthResponse;
import com.gp.radioregistry.security.auth.service.AuthenticationService;
import com.gp.radioregistry.user.dto.response.UserResponse;
import com.gp.radioregistry.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
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
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterUserRequest request) {
        log.info("Creation request received for user with username: {}", request.username());
        var user = userService.createUser(request);

        return ResponseEntity.created(URI.create(String.format("%s/%d", USERS_PATH, user.getId())))
            .body(new AuthResponse(UserResponse.fromEntity(user), Instant.now()));
    }

    @PostMapping("/login")
    @Operation(summary = "Performs login", description = "Authenticates a user and returns a response with user details.")
    public ResponseEntity<AuthResponse> doLogin(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest servletRequest) {
        var authentication = authenticationService.doAuthentication(loginRequest);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        servletRequest.getSession().setAttribute(
            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
            SecurityContextHolder.getContext());

        var user = userService.findByUsernameOrEmail(authentication.getName());

        return ResponseEntity.ok(new AuthResponse(UserResponse.fromEntity(user), Instant.now()));
    }
}

