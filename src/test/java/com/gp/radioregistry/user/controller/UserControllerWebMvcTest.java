package com.gp.radioregistry.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gp.radioregistry.role.domain.Role;
import com.gp.radioregistry.user.domain.User;
import com.gp.radioregistry.user.dto.request.UpdateUserPasswordRequest;
import com.gp.radioregistry.user.dto.request.UpdateUserRequest;
import com.gp.radioregistry.user.dto.request.UpdateUserRolesRequest;
import com.gp.radioregistry.user.service.UserService;
import com.gp.radioregistry.security.config.SecurityConfig;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static com.gp.radioregistry.constant.ApiConstants.USERS_PATH;
import static com.gp.radioregistry.security.enums.Role.ADMIN;
import static com.gp.radioregistry.security.enums.Role.OPERATOR;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@DisplayName("UserController @WebMvcTest")
class UserControllerWebMvcTest {

    private static final Long USER_ID = 1L;
    private static final Long USER_ID_NOT_FOUND = 99L;
    private static final Long ROLE_ID = 3L;

    private static final String USERNAME = "john.doe";
    private static final String EMAIL = "john.doe@example.com";
    private static final String PASSWORD = "Str0ngPass1";
    private static final String ROLE_NAME = "OPERATOR";

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();

        OffsetDateTime now = OffsetDateTime.now();

        Role role = Role.builder().id(ROLE_ID).name(ROLE_NAME).build();

        user = User.builder()
                .id(USER_ID)
                .username(USERNAME)
                .email(EMAIL)
                .password("encoded-password")
                .roles(Set.of(role))
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Nested
    @DisplayName("Security (admin-only endpoints)")
    class Security {

        @Test
        @DisplayName("GET returns 401 when unauthenticated")
        void getUnauthenticatedReturns401() throws Exception {
            mockMvc.perform(get(USERS_PATH))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET returns 403 for non-admin (OPERATOR) role")
        void getWithOperatorReturns403() throws Exception {
            mockMvc.perform(get(USERS_PATH)
                            .with(user("operator").roles(OPERATOR.getName())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("PUT returns 401 when unauthenticated")
        void updateUnauthenticatedReturns401() throws Exception {
            var request = new UpdateUserRequest(USERNAME, null);

            mockMvc.perform(put(USERS_PATH + "/{id}", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("PUT returns 403 for non-admin (OPERATOR) role")
        void updateWithOperatorReturns403() throws Exception {
            var request = new UpdateUserRequest(USERNAME, null);

            mockMvc.perform(put(USERS_PATH + "/{id}", USER_ID)
                            .with(user("operator").roles(OPERATOR.getName()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("DELETE returns 403 for non-admin (OPERATOR) role")
        void deleteWithOperatorReturns403() throws Exception {
            mockMvc.perform(delete(USERS_PATH + "/{id}", USER_ID)
                            .with(user("operator").roles(OPERATOR.getName())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET is allowed for ADMIN role")
        void getWithAdminIsAllowed() throws Exception {
            when(userService.getUserById(USER_ID)).thenReturn(user);

            mockMvc.perform(get(USERS_PATH + "/{id}", USER_ID)
                            .with(user("admin").roles(ADMIN.getName())))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Bean validation")
    class Validation {

        @Test
        @DisplayName("PUT returns 400 when username is too short")
        void updateTooShortUsernameReturns400() throws Exception {
            var invalid = new UpdateUserRequest("ab", null);

            mockMvc.perform(put(USERS_PATH + "/{id}", USER_ID)
                            .with(user("admin").roles(ADMIN.getName()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Content-Type", "application/problem+json"))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors.username").exists());
        }

        @Test
        @DisplayName("PUT /password returns 400 when password is blank")
        void updatePasswordBlankReturns400() throws Exception {
            var invalid = new UpdateUserPasswordRequest("  ");

            mockMvc.perform(put(USERS_PATH + "/{id}/password", USER_ID)
                            .with(user("admin").roles(ADMIN.getName()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.password").exists());
        }

        @Test
        @DisplayName("PUT /roles returns 400 when roleIds is empty")
        void updateRolesEmptyReturns400() throws Exception {
            var invalid = new UpdateUserRolesRequest(Set.of());

            mockMvc.perform(put(USERS_PATH + "/{id}/roles", USER_ID)
                            .with(user("admin").roles(ADMIN.getName()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.roleIds").exists());
        }
    }

    @Nested
    @DisplayName("Exception mapping")
    class ExceptionMapping {

        @Test
        @DisplayName("GET by id maps EntityNotFoundException to 404 ProblemDetail")
        void getByIdNotFoundReturns404() throws Exception {
            when(userService.getUserById(USER_ID_NOT_FOUND))
                    .thenThrow(new EntityNotFoundException("User not found with id: " + USER_ID_NOT_FOUND));

            mockMvc.perform(get(USERS_PATH + "/{id}", USER_ID_NOT_FOUND)
                            .with(user("admin").roles(ADMIN.getName())))
                    .andExpect(status().isNotFound())
                    .andExpect(header().string("Content-Type", "application/problem+json"))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.title").value("Resource not found"))
                    .andExpect(jsonPath("$.detail").value("User not found with id: " + USER_ID_NOT_FOUND))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("DELETE returns 204 No Content on success (no response body)")
        void deleteReturns204() throws Exception {
            mockMvc.perform(delete(USERS_PATH + "/{id}", USER_ID)
                            .with(user("admin").roles(ADMIN.getName())))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(userService).deleteUser(USER_ID);
        }

        @Test
        @DisplayName("DELETE maps EntityNotFoundException to 404 ProblemDetail")
        void deleteNotFoundReturns404() throws Exception {
            doThrow(new EntityNotFoundException("User not found with ID: " + USER_ID_NOT_FOUND))
                    .when(userService).deleteUser(USER_ID_NOT_FOUND);

            mockMvc.perform(delete(USERS_PATH + "/{id}", USER_ID_NOT_FOUND)
                            .with(user("admin").roles(ADMIN.getName())))
                    .andExpect(status().isNotFound())
                    .andExpect(header().string("Content-Type", "application/problem+json"))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.title").value("Resource not found"))
                    .andExpect(jsonPath("$.detail").value("User not found with ID: " + USER_ID_NOT_FOUND))
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    @DisplayName("JSON response shape & headers")
    class ResponseShape {

        @Test
        @DisplayName("PUT returns 200 with mapped body (never exposes the password)")
        void updateReturns200WithBody() throws Exception {
            var request = new UpdateUserRequest(USERNAME, null);
            when(userService.updateUser(anyLong(), any(UpdateUserRequest.class))).thenReturn(user);

            mockMvc.perform(put(USERS_PATH + "/{id}", USER_ID)
                            .with(user("admin").roles(ADMIN.getName()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(USER_ID))
                    .andExpect(jsonPath("$.username").value(USERNAME))
                    .andExpect(jsonPath("$.email").value(EMAIL))
                    .andExpect(jsonPath("$.password").doesNotExist())
                    .andExpect(jsonPath("$.roles").isArray())
                    .andExpect(jsonPath("$.roles[0].id").value(ROLE_ID));
        }

        @Test
        @DisplayName("PUT /password returns 204 No Content on success")
        void updatePasswordReturns204() throws Exception {
            var request = new UpdateUserPasswordRequest(PASSWORD);

            mockMvc.perform(put(USERS_PATH + "/{id}/password", USER_ID)
                            .with(user("admin").roles(ADMIN.getName()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(userService).updateUserPassword(anyLong(), any(UpdateUserPasswordRequest.class));
        }

        @Test
        @DisplayName("PUT /roles returns 200 with mapped body")
        void updateRolesReturns200WithBody() throws Exception {
            var request = new UpdateUserRolesRequest(Set.of(ROLE_ID));
            when(userService.updateUserRoles(anyLong(), any(UpdateUserRolesRequest.class))).thenReturn(user);

            mockMvc.perform(put(USERS_PATH + "/{id}/roles", USER_ID)
                            .with(user("admin").roles(ADMIN.getName()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(USER_ID))
                    .andExpect(jsonPath("$.roles").isArray())
                    .andExpect(jsonPath("$.roles[0].name").value(ROLE_NAME));
        }

        @Test
        @DisplayName("GET by id returns 200 with mapped body")
        void getByIdReturnsMappedBody() throws Exception {
            when(userService.getUserById(USER_ID)).thenReturn(user);

            mockMvc.perform(get(USERS_PATH + "/{id}", USER_ID)
                            .with(user("admin").roles(ADMIN.getName())))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(USER_ID))
                    .andExpect(jsonPath("$.username").value(USERNAME))
                    .andExpect(jsonPath("$.email").value(EMAIL))
                    .andExpect(jsonPath("$.enabled").value(true))
                    .andExpect(jsonPath("$.accountNonLocked").value(true))
                    .andExpect(jsonPath("$.password").doesNotExist());
        }

        @Test
        @DisplayName("GET (list) returns 200 with a paginated JSON body (content + page metadata)")
        void getUsersReturnsPagedJson() throws Exception {
            Pageable pageable = PageRequest.of(0, 20);
            Page<User> page = new PageImpl<>(List.of(user), pageable, 1);
            when(userService.getUsers(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get(USERS_PATH)
                            .with(user("admin").roles(ADMIN.getName())))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(USER_ID))
                    .andExpect(jsonPath("$.content[0].username").value(USERNAME))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.size").value(20));
        }

        @Test
        @DisplayName("GET (list) returns 200 with an empty content array when there are no users")
        void getUsersReturnsEmptyPagedJson() throws Exception {
            Pageable pageable = PageRequest.of(0, 20);
            when(userService.getUsers(any(Pageable.class))).thenReturn(Page.empty(pageable));

            mockMvc.perform(get(USERS_PATH)
                            .with(user("admin").roles(ADMIN.getName())))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }
}

