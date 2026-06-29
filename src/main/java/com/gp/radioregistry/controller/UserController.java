package com.gp.radioregistry.controller;

import com.gp.radioregistry.request.UpdateUserPasswordRequest;
import com.gp.radioregistry.request.UpdateUserRequest;
import com.gp.radioregistry.request.UpdateUserRolesRequest;
import com.gp.radioregistry.response.UserResponse;
import com.gp.radioregistry.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.gp.radioregistry.constant.AppConstants.Api.USERS_PATH;

@Log4j2
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
    public ResponseEntity<UserResponse> updateUserPassword(@PathVariable Long id, @Valid @RequestBody UpdateUserPasswordRequest request) {
        log.info("Update password request received for user with id: {}", id);

        var user = userService.updateUserPassword(id, request);

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
    public ResponseEntity<List<UserResponse>> getUsers() {
        log.info("Request received to fetch all users");

        var users = userService.getUsers();

        return ResponseEntity.ok(users.stream().map(UserResponse::fromEntity).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by id", description = "Returns a single user matching the given id. Admin-only")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("Request received to fetch user with id: {}", id);

        var user = userService.getUserById(id);

        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }
}

