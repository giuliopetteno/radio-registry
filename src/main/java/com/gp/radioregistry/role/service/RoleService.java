package com.gp.radioregistry.role.service;

import com.gp.radioregistry.audit.annotation.Auditable;
import com.gp.radioregistry.enums.EntityType;
import com.gp.radioregistry.enums.EventType;
import com.gp.radioregistry.exception.ResourceAlreadyExistsException;
import com.gp.radioregistry.role.domain.Role;
import com.gp.radioregistry.role.dto.request.CreateRoleRequest;
import com.gp.radioregistry.role.dto.request.UpdateRoleRequest;
import com.gp.radioregistry.role.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    @Auditable(eventType = EventType.CREATE, entityType = EntityType.ROLE, entityId = "#result.id", description = "Role creation attempt")
    public Role createRole(CreateRoleRequest request) {
        var roleName = request.name().trim().toUpperCase();

        if (roleRepository.existsByName(roleName)) {
            throw new ResourceAlreadyExistsException("Role with name " + request.name() + " already exists");
        }

        var role = Role.builder()
                .name(roleName)
                .build();
        return roleRepository.save(role);
    }

    @Auditable(eventType = EventType.UPDATE, entityType = EntityType.ROLE, entityId = "#id", description = "Role update attempt")
    public Role updateRole(Long id, UpdateRoleRequest request) {
        var role = getRoleById(id);
        Optional.ofNullable(request.name()).ifPresent(role::setName);

        return roleRepository.save(role);
    }

    @Auditable(eventType = EventType.DELETE, entityType = EntityType.ROLE, entityId = "#id", description = "Role deletion attempt")
    public void deleteRole(Long id) {
        var role = roleRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));
        roleRepository.delete(role);
    }

    public Page<Role> getRoles(Pageable pageable) {
        return roleRepository.findAll(pageable);
    }

    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with ID: " + id));
    }
}