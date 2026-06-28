package com.gp.radioregistry.service;

import com.gp.radioregistry.domain.User;
import com.gp.radioregistry.exception.ResourceAlreadyExistsException;
import com.gp.radioregistry.repository.RoleRepository;
import com.gp.radioregistry.repository.UserRepository;
import com.gp.radioregistry.request.RegisterUserRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.gp.radioregistry.constant.AppConstants.Security.ROLE_USER;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(RegisterUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResourceAlreadyExistsException("User with username " + request.username() + " already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ResourceAlreadyExistsException("User with email " + request.email() + " already exists");
        }

        var defaultRole = roleRepository.findByName(ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("Default role " + ROLE_USER + " not found"));

        var user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(new HashSet<>(Set.of(defaultRole)))
                .build();
        return userRepository.save(user);
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