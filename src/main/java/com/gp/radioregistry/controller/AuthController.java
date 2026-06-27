package com.gp.radioregistry.controller;

import com.gp.radioregistry.request.LoginRequest;
import com.gp.radioregistry.request.RegisterUserRequest;
import com.gp.radioregistry.response.UserResponse;
import com.gp.radioregistry.service.AuthenticationService;
import com.gp.radioregistry.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/auth")
@Tag(name = "Auth controller", description = "API for authentication and authorization")
public class AuthController {
    private final AuthenticationService authenticationService;
    private final UserService userService; // Assuming you have this

    // 2. Inject it via the constructor
    public AuthController(AuthenticationService authenticationService, UserService userService) {
        this.authenticationService = authenticationService;
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Receives a new user, validates it and register it.")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterUserRequest request) {
        log.info("Creation request received for user with username: {}", request.username());
        var user = userService.createUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromEntity(user));
    }

    @PostMapping("/login")
    @Operation(summary = "Performs login", description = "Authenticates a user and returns a response with user details.")
    public ResponseEntity<UserResponse> doLogin(@RequestBody LoginRequest loginRequest, HttpServletRequest servletRequest) {
        var authentication = authenticationService.doAuthentication(loginRequest, servletRequest);

        if(authentication != null && authentication.isAuthenticated()) {
            var user = userService.findByUsernameOrEmail(authentication.getName());

            return ResponseEntity.status(HttpStatus.OK).body(UserResponse.fromEntity(user));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}

