package com.gp.radioregistry.user.service;

import com.gp.radioregistry.audit.annotation.Auditable;
import com.gp.radioregistry.audit.enums.AuditAction;
import com.gp.radioregistry.audit.enums.AuditEntityType;
import com.gp.radioregistry.exception.ResourceAlreadyExistsException;
import com.gp.radioregistry.role.repository.RoleRepository;
import com.gp.radioregistry.security.auth.dto.request.RegisterUserRequest;
import com.gp.radioregistry.security.enums.Role;
import com.gp.radioregistry.user.domain.User;
import com.gp.radioregistry.user.dto.request.UpdateUserPasswordRequest;
import com.gp.radioregistry.user.dto.request.UpdateUserRequest;
import com.gp.radioregistry.user.dto.request.UpdateUserRolesRequest;
import com.gp.radioregistry.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Auditable(action = AuditAction.CREATE, entityType = AuditEntityType.USER, entityId = "#result.id")
    public User createUser(RegisterUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResourceAlreadyExistsException("User with username " + request.username() + " already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ResourceAlreadyExistsException("User with email " + request.email() + " already exists");
        }

        var defaultRole = Role.OPERATOR.getName();
        var role = roleRepository.findByName(defaultRole)
                .orElseThrow(() -> new IllegalStateException("Default role " + defaultRole + " not found"));

        var user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(new HashSet<>(Set.of(role)))
                .build();
        return userRepository.save(user);
    }

    @Auditable(action = AuditAction.UPDATE, entityType = AuditEntityType.USER, entityId = "#id")
    public User updateUser(Long id, UpdateUserRequest request) {
        var user = getUserById(id);
        Optional.ofNullable(request.username()).ifPresent(user::setUsername);
        Optional.ofNullable(request.email()).ifPresent(user::setEmail);

        return userRepository.save(user);
    }

    @Auditable(action = AuditAction.UPDATE, entityType = AuditEntityType.USER_PASSWORD, entityId = "#id")
    public void updateUserPassword(Long id, UpdateUserPasswordRequest request) {
        var user = getUserById(id);
        user.setPassword(passwordEncoder.encode(request.password()));

        userRepository.save(user);
    }

    @Auditable(action = AuditAction.UPDATE, entityType = AuditEntityType.USER_ROLES, entityId = "#id")
    public User updateUserRoles(Long id, UpdateUserRolesRequest request) {
        var user = getUserById(id);

        user.setRoles(request.roleIds().stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new EntityNotFoundException("Role not found with ID: " + roleId)))
                .collect(Collectors.toSet()));

        return userRepository.save(user);
    }

    @Auditable(action = AuditAction.DELETE, entityType = AuditEntityType.USER, entityId = "#id")
    public void deleteUser(Long id) {
        var user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }

    public User findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));
    }
}