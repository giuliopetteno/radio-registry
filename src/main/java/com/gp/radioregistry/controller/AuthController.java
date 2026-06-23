package com.gp.radioregistry.controller;

import com.gp.radioregistry.domain.User;
import com.gp.radioregistry.request.CreateUserRequest;
import com.gp.radioregistry.response.UserResponse;
import com.gp.radioregistry.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/auth")
@Tag(name = "Auth controller", description = "API for authentication and authorization")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Receives a new user, validates it and register it.")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Creation request received for user with username: {}", request.username());

        User user = userService.createUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromEntity(user));
    }

/*    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }*/
}

