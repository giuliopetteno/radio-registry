package com.gp.radioregistry.service;

import com.gp.radioregistry.domain.Role;
import com.gp.radioregistry.exception.ResourceAlreadyExistsException;
import com.gp.radioregistry.repository.RoleRepository;
import com.gp.radioregistry.request.CreateRoleRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

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

    public List<Role> getRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with ID: " + id));
    }
}