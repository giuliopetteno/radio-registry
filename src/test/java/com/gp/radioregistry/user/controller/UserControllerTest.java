package com.gp.radioregistry.user.controller;

import com.gp.radioregistry.user.domain.User;
import com.gp.radioregistry.user.dto.request.UpdateUserPasswordRequest;
import com.gp.radioregistry.user.dto.request.UpdateUserRequest;
import com.gp.radioregistry.user.dto.request.UpdateUserRolesRequest;
import com.gp.radioregistry.user.dto.response.UserResponse;
import com.gp.radioregistry.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController unit tests")
class UserControllerTest {

    private static final Long USER_ID = 1L;
    private static final Long USER_ID_NOT_FOUND = 99L;
    private static final String USERNAME = "user";
    private static final String EMAIL = "user@example.com";
    private static final String PASSWORD = "password";
    private static final Set<Long> USER_ROLES = Set.of(1L, 2L);

    private static final String USERNAME_UPDATE = "updated_user";
    private static final String PASSWORD_UPDATE = "updated_password";

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(USER_ID)
                .username(USERNAME)
                .email(EMAIL)
                .password(PASSWORD)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("updateUser")
    class UpdateUser {

        @Test
        @DisplayName("should return 200 OK with the updated user")
        void updateUser_returnsOk() {
            var request = new UpdateUserRequest(USERNAME_UPDATE, null);
            user.setUsername(USERNAME_UPDATE);
            when(userService.updateUser(USER_ID, request)).thenReturn(user);

            ResponseEntity<UserResponse> response = userController.updateUser(USER_ID, request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(USER_ID, response.getBody().id());
            assertEquals(USERNAME_UPDATE, response.getBody().username());
            verify(userService).updateUser(USER_ID, request);
        }

        @Test
        @DisplayName("should propagate EntityNotFoundException when user does not exist")
        void updateUser_notFound() {
            var request = new UpdateUserRequest(USERNAME_UPDATE, null);
            when(userService.updateUser(anyLong(), any(UpdateUserRequest.class)))
                    .thenThrow(new EntityNotFoundException("User not found"));

            assertThrows(EntityNotFoundException.class, () -> userController.updateUser(USER_ID_NOT_FOUND, request));
        }
    }

    @Nested
    @DisplayName("updateUserPassword")
    class UpdateUserPassword {

        @Test
        @DisplayName("should return 204 No Content and delegate to service")
        void updateUserPassword_returnsNoContent() {
            var request = new UpdateUserPasswordRequest(PASSWORD_UPDATE);

            ResponseEntity<Void> response = userController.updateUserPassword(USER_ID, request);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertNull(response.getBody());
            verify(userService).updateUserPassword(USER_ID, request);
        }
    }

    @Nested
    @DisplayName("updateUserRoles")
    class UpdateUserRoles {

        @Test
        @DisplayName("should return 200 OK with the updated user")
        void updateUserRoles_returnsOk() {
            var request = new UpdateUserRolesRequest(USER_ROLES);
            when(userService.updateUserRoles(USER_ID, request)).thenReturn(user);

            ResponseEntity<UserResponse> response = userController.updateUserRoles(USER_ID, request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(USER_ID, response.getBody().id());
            verify(userService).updateUserRoles(USER_ID, request);
        }
    }

    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("should return 204 No Content and delegate to service")
        void deleteUser_returnsNoContent() {
            ResponseEntity<Void> response = userController.deleteUser(USER_ID);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertNull(response.getBody());
            verify(userService).deleteUser(USER_ID);
        }

        @Test
        @DisplayName("should propagate EntityNotFoundException when user does not exist")
        void deleteUser_notFound() {
            doThrow(new EntityNotFoundException("User not found")).when(userService).deleteUser(USER_ID_NOT_FOUND);

            assertThrows(EntityNotFoundException.class, () -> userController.deleteUser(USER_ID_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("getUsers")
    class GetUsers {

        @Test
        @DisplayName("should return 200 OK with a mapped page of users")
        void getUsers_returnsPage() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<User> page = new PageImpl<>(List.of(user), pageable, 1);
            when(userService.getUsers(pageable)).thenReturn(page);

            ResponseEntity<Page<UserResponse>> response = userController.getUsers(pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getTotalElements());
            assertEquals(USER_ID, response.getBody().getContent().getFirst().id());
            verify(userService).getUsers(pageable);
        }

        @Test
        @DisplayName("should return 200 OK with an empty page when no users exist")
        void getUsers_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 20);
            when(userService.getUsers(pageable)).thenReturn(Page.empty(pageable));

            ResponseEntity<Page<UserResponse>> response = userController.getUsers(pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());
        }
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserById {

        @Test
        @DisplayName("should return 200 OK with the mapped user")
        void getUserById_returnsOk() {
            when(userService.getUserById(USER_ID)).thenReturn(user);

            ResponseEntity<UserResponse> response = userController.getUserById(USER_ID);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(USER_ID, response.getBody().id());
            assertEquals(USERNAME, response.getBody().username());
            assertEquals(EMAIL, response.getBody().email());
            verify(userService).getUserById(USER_ID);
        }

        @Test
        @DisplayName("should propagate EntityNotFoundException when user does not exist")
        void getUserById_notFound() {
            when(userService.getUserById(USER_ID_NOT_FOUND)).thenThrow(new EntityNotFoundException("User not found"));

            assertThrows(EntityNotFoundException.class, () -> userController.getUserById(USER_ID_NOT_FOUND));
        }
    }
}

