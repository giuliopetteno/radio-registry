package com.gp.radioregistry.user.controller;

import com.gp.radioregistry.user.dto.request.UpdateUserPasswordRequest;
import com.gp.radioregistry.user.dto.request.UpdateUserRequest;
import com.gp.radioregistry.user.dto.request.UpdateUserRolesRequest;
import com.gp.radioregistry.user.dto.response.UserResponse;
import com.gp.radioregistry.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.gp.radioregistry.constant.ApiConstants.USERS_PATH;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(USERS_PATH)
@Tag(name = "Users controller", description = "API for managing users")
public class UserController {
    private final UserService userService;

    @PutMapping("/{id}")
    @Operation(summary = "Update request for user", description = "Updates a user. Admin-only")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        log.info("Update request received for user with id: {}", id);

        var user = userService.updateUser(id, request);

        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @PutMapping("/{id}/password")
    @Operation(summary = "Update request for password of a user", description = "Updates the password of a user. Admin-only")
    public ResponseEntity<Void> updateUserPassword(@PathVariable Long id, @Valid @RequestBody UpdateUserPasswordRequest request) {
        log.info("Update password request received for user with id: {}", id);

        userService.updateUserPassword(id, request);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "Update request for roles of a user", description = "Updates the roles of a user. Admin-only")
    public ResponseEntity<UserResponse> updateUserRoles(@PathVariable Long id, @Valid @RequestBody UpdateUserRolesRequest request) {
        log.info("Update roles request received for user with id: {}", id);

        var user = userService.updateUserRoles(id, request);

        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete request for user", description = "Deletes a user by ID. Admin-only")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Delete request received for user with id: {}", id);

        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "List all users", description = "Returns the complete list of users available in the system. Admin-only")
    public ResponseEntity<Page<UserResponse>> getUsers(@ParameterObject Pageable pageable) {
        log.info("Request received to fetch all users");

        var users = userService.getUsers(pageable);

        return ResponseEntity.ok(users.map(UserResponse::fromEntity));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by id", description = "Returns a single user matching the given id. Admin-only")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("Request received to fetch user with id: {}", id);

        var user = userService.getUserById(id);

        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }
}

