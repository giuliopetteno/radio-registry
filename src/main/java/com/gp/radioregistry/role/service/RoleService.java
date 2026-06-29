package com.gp.radioregistry.role.service;

import com.gp.radioregistry.role.domain.Role;
import com.gp.radioregistry.exception.ResourceAlreadyExistsException;
import com.gp.radioregistry.role.repository.RoleRepository;
import com.gp.radioregistry.role.dto.request.CreateRoleRequest;
import com.gp.radioregistry.role.dto.request.UpdateRoleRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

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

    public Role updateRole(Long id, UpdateRoleRequest request) {
        var role = getRoleById(id);
        Optional.ofNullable(request.name()).ifPresent(role::setName);

        return roleRepository.save(role);
    }

    public void deleteRole(Long id) {
        var role = roleRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));
        roleRepository.delete(role);
    }

    public List<Role> getRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with ID: " + id));
    }
}