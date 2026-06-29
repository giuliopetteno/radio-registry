package com.gp.radioregistry.role.controller;

import com.gp.radioregistry.role.dto.request.CreateRoleRequest;
import com.gp.radioregistry.role.dto.request.UpdateRoleRequest;
import com.gp.radioregistry.role.dto.response.RoleResponse;
import com.gp.radioregistry.role.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static com.gp.radioregistry.constant.ApiConstants.ROLES_PATH;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ROLES_PATH)
@Tag(name = "Roles controller", description = "API for managing roles")
public class RoleController {
    private final RoleService roleService;

    @PostMapping
    @Operation(summary = "Create a new role", description = "Receives a new role, validates it and saves it. Admin-only")
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        log.info("Creation request received for role with name: {}", request.name());

        var role = roleService.createRole(request);

        return ResponseEntity.created(URI.create(String.format("%s/%d", ROLES_PATH, role.getId()))).body(RoleResponse.fromEntity(role));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update request for role", description = "Updates a role. Admin-only")
    public ResponseEntity<RoleResponse> updateRole(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
        log.info("Update request received for role with id: {}", id);

        var role = roleService.updateRole(id, request);

        return ResponseEntity.ok(RoleResponse.fromEntity(role));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete request for role", description = "Deletes a role by ID. Admin-only")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        log.info("Delete request received for role with id: {}", id);

        roleService.deleteRole(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "List all roles", description = "Returns the complete list of roles available in the system. Admin-only")
    public ResponseEntity<List<RoleResponse>> getRoles() {
        log.info("Request received to fetch all roles");

        var roles = roleService.getRoles();

        return ResponseEntity.ok(roles.stream().map(RoleResponse::fromEntity).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get role by id", description = "Returns a single role matching the given id. Admin-only")
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable Long id) {
        log.info("Request received to fetch role with id: {}", id);

        var role = roleService.getRoleById(id);

        return ResponseEntity.ok(RoleResponse.fromEntity(role));
    }
}

