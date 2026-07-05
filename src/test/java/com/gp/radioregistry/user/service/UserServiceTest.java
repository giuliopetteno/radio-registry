package com.gp.radioregistry.user.service;

import com.gp.radioregistry.exception.ResourceAlreadyExistsException;
import com.gp.radioregistry.role.domain.Role;
import com.gp.radioregistry.role.repository.RoleRepository;
import com.gp.radioregistry.security.auth.dto.request.RegisterUserRequest;
import com.gp.radioregistry.user.domain.User;
import com.gp.radioregistry.user.dto.request.UpdateUserPasswordRequest;
import com.gp.radioregistry.user.dto.request.UpdateUserRequest;
import com.gp.radioregistry.user.dto.request.UpdateUserRolesRequest;
import com.gp.radioregistry.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService unit tests")
class UserServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long USER_ID_NOT_FOUND = 99L;
    private static final String USERNAME = "user";
    private static final String EMAIL = "user@example.com";
    private static final String RAW_PASSWORD = "password";
    private static final String ENCODED_PASSWORD = "encoded_password";
    private static final String ROLE_NAME = "OPERATOR";

    private static final String USERNAME_UPDATE = "updated_user";
    private static final String EMAIL_UPDATE = "updated@example.com";
    private static final String PASSWORD_UPDATE = "updated_password";

    private static final String USERNAME_WRONG = "wrong_user";
    private static final String EMAIL_WRONG = "wrong@example.com";

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(USER_ID)
                .username(USERNAME)
                .email(EMAIL)
                .password(ENCODED_PASSWORD)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("createUser")
    class CreateUser {

        @Test
        @DisplayName("should encode password, assign default OPERATOR role and save")
        void createUser_savesUser() {
            var request = new RegisterUserRequest(USERNAME, EMAIL, RAW_PASSWORD);
            var operatorRole = Role.builder().id(3L).name(ROLE_NAME).build();
            when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(roleRepository.findByName(ROLE_NAME)).thenReturn(Optional.of(operatorRole));
            when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(userRepository.save(any(User.class))).thenReturn(user);

            User result = userService.createUser(request);

            assertSame(user, result);
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            User saved = captor.getValue();
            assertEquals(USERNAME, saved.getUsername());
            assertEquals(EMAIL, saved.getEmail());
            assertEquals(ENCODED_PASSWORD, saved.getPassword());
            assertTrue(saved.getRoles().contains(operatorRole));
        }

        @Test
        @DisplayName("should throw ResourceAlreadyExistsException when username exists")
        void createUser_usernameExists() {
            var request = new RegisterUserRequest(USERNAME, EMAIL, RAW_PASSWORD);
            when(userRepository.existsByUsername(USERNAME)).thenReturn(true);

            assertThrows(ResourceAlreadyExistsException.class, () -> userService.createUser(request));
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ResourceAlreadyExistsException when email exists")
        void createUser_emailExists() {
            var request = new RegisterUserRequest(USERNAME, EMAIL, RAW_PASSWORD);
            when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

            assertThrows(ResourceAlreadyExistsException.class, () -> userService.createUser(request));
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw IllegalStateException when default OPERATOR role is missing")
        void createUser_defaultRoleMissing() {
            var request = new RegisterUserRequest(USERNAME, EMAIL, RAW_PASSWORD);
            when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(roleRepository.findByName(ROLE_NAME)).thenReturn(Optional.empty());

            assertThrows(IllegalStateException.class, () -> userService.createUser(request));
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateUser")
    class UpdateUser {

        @Test
        @DisplayName("should update username and email when present and save")
        void updateUser_updatesAndSaves() {
            var request = new UpdateUserRequest(USERNAME_UPDATE, EMAIL_UPDATE);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);

            User result = userService.updateUser(USER_ID, request);

            assertEquals(USERNAME_UPDATE, result.getUsername());
            assertEquals(EMAIL_UPDATE, result.getEmail());
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("should keep existing values when request fields are null")
        void updateUser_keepsExistingWhenNull() {
            var request = new UpdateUserRequest(null, null);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);

            User result = userService.updateUser(USER_ID, request);

            assertEquals(USERNAME, result.getUsername());
            assertEquals(EMAIL, result.getEmail());
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when user does not exist")
        void updateUser_notFound() {
            var request = new UpdateUserRequest(USERNAME_UPDATE, null);
            when(userRepository.findById(USER_ID_NOT_FOUND)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> userService.updateUser(USER_ID_NOT_FOUND, request));
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateUserPassword")
    class UpdateUserPassword {

        @Test
        @DisplayName("should encode the new password and save")
        void updateUserPassword_encodesAndSaves() {
            var request = new UpdateUserPasswordRequest(PASSWORD_UPDATE);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(passwordEncoder.encode(PASSWORD_UPDATE)).thenReturn("new-encoded");
            when(userRepository.save(user)).thenReturn(user);

            userService.updateUserPassword(USER_ID, request);

            assertEquals("new-encoded", user.getPassword());
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when user does not exist")
        void updateUserPassword_notFound() {
            var request = new UpdateUserPasswordRequest(PASSWORD_UPDATE);
            when(userRepository.findById(USER_ID_NOT_FOUND)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> userService.updateUserPassword(USER_ID_NOT_FOUND, request));
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateUserRoles")
    class UpdateUserRoles {

        @Test
        @DisplayName("should resolve roles by id, set them and save")
        void updateUserRoles_updatesAndSaves() {
            var request = new UpdateUserRolesRequest(Set.of(1L));
            var role = Role.builder().id(1L).name("ADMIN").build();
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
            when(userRepository.save(user)).thenReturn(user);

            User result = userService.updateUserRoles(USER_ID, request);

            assertTrue(result.getRoles().contains(role));
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when a role id is unknown")
        void updateUserRoles_roleNotFound() {
            var request = new UpdateUserRolesRequest(Set.of(99L));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(roleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> userService.updateUserRoles(USER_ID, request));
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when user does not exist")
        void updateUserRoles_userNotFound() {
            var request = new UpdateUserRolesRequest(Set.of(1L));
            when(userRepository.findById(USER_ID_NOT_FOUND)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> userService.updateUserRoles(USER_ID_NOT_FOUND, request));
        }
    }

    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("should delete the user when it exists")
        void deleteUser_deletes() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            userService.deleteUser(USER_ID);

            verify(userRepository).delete(user);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when user does not exist")
        void deleteUser_notFound() {
            when(userRepository.findById(USER_ID_NOT_FOUND)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> userService.deleteUser(USER_ID_NOT_FOUND));
            verify(userRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("findByUsernameOrEmail")
    class FindByUsernameOrEmail {

        @Test
        @DisplayName("should return the user when found")
        void findByUsernameOrEmail_returns() {
            when(userRepository.findByUsernameOrEmail(USERNAME, USERNAME)).thenReturn(Optional.of(user));

            User result = userService.findByUsernameOrEmail(USERNAME);

            assertSame(user, result);
        }

        @Test
        @DisplayName("should throw UsernameNotFoundException when not found")
        void findByUsernameOrEmail_notFound() {
            when(userRepository.findByUsernameOrEmail(USERNAME_WRONG, EMAIL_WRONG)).thenReturn(Optional.empty());

            assertThrows(UsernameNotFoundException.class,
                    () -> userService.findByUsernameOrEmail(USERNAME_WRONG));
        }
    }

    @Nested
    @DisplayName("getUsers")
    class GetUsers {

        @Test
        @DisplayName("should return the page returned by the repository")
        void getUsers_returnsPage() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<User> page = new PageImpl<>(List.of(user), pageable, 1);
            when(userRepository.findAll(pageable)).thenReturn(page);

            Page<User> result = userService.getUsers(pageable);

            assertEquals(1, result.getTotalElements());
            assertSame(user, result.getContent().getFirst());
        }
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserById {

        @Test
        @DisplayName("should return the user when it exists")
        void getUserById_returns() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            User result = userService.getUserById(USER_ID);

            assertSame(user, result);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when user does not exist")
        void getUserById_notFound() {
            when(userRepository.findById(USER_ID_NOT_FOUND)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> userService.getUserById(USER_ID_NOT_FOUND));
        }
    }
}

