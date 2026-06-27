package com.gp.radioregistry.controller;

import com.gp.radioregistry.request.CreateRoleRequest;
import com.gp.radioregistry.response.RoleResponse;
import com.gp.radioregistry.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/roles")
@Tag(name = "Roles controller", description = "API for managing roles")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    @Operation(summary = "Create a new role", description = "Receives a new role, validates it and saves it.")
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        log.info("Creation request received for role with name: {}", request.name());

        var role = roleService.createRole(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(RoleResponse.fromEntity(role));
    }
}

