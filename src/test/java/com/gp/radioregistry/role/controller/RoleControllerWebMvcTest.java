package com.gp.radioregistry.role.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gp.radioregistry.role.domain.Role;
import com.gp.radioregistry.role.dto.request.CreateRoleRequest;
import com.gp.radioregistry.role.dto.request.UpdateRoleRequest;
import com.gp.radioregistry.role.service.RoleService;
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

import static com.gp.radioregistry.constant.ApiConstants.ROLES_PATH;
import static com.gp.radioregistry.security.enums.Role.ADMIN;
import static com.gp.radioregistry.security.enums.Role.OPERATOR;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoleController.class)
@Import(SecurityConfig.class)
@DisplayName("RoleController @WebMvcTest")
class RoleControllerWebMvcTest {

    private static final Long ROLE_ID = 3L;
    private static final Long ROLE_ID_NOT_FOUND = 99L;

    private static final String ROLE_NAME = "AUDITOR";

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private RoleService roleService;

    private Role role;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();

        OffsetDateTime now = OffsetDateTime.now();

        role = Role.builder()
                .id(ROLE_ID)
                .name(ROLE_NAME)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private CreateRoleRequest validCreateRequest() {
        return new CreateRoleRequest(ROLE_NAME);
    }

    @Nested
    @DisplayName("Security (admin-only endpoints)")
    class Security {

        @Test
        @DisplayName("GET returns 401 when unauthenticated")
        void getUnauthenticatedReturns401() throws Exception {
            mockMvc.perform(get(ROLES_PATH))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET returns 403 for non-admin (OPERATOR) role")
        void getWithOperatorReturns403() throws Exception {
            mockMvc.perform(get(ROLES_PATH)
                            .with(user("operator").roles(OPERATOR.getName())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST returns 401 when unauthenticated")
        void createUnauthenticatedReturns401() throws Exception {
            mockMvc.perform(post(ROLES_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest())))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST returns 403 for non-admin (OPERATOR) role")
        void createWithOperatorReturns403() throws Exception {
            mockMvc.perform(post(ROLES_PATH)
                            .with(user("operator").roles(OPERATOR.getName()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("PUT returns 403 for non-admin (OPERATOR) role")
        void updateWithOperatorReturns403() throws Exception {
            var request = new UpdateRoleRequest(ROLE_NAME);

            mockMvc.perform(put(ROLES_PATH + "/{id}", ROLE_ID)
                            .with(user("operator").roles(OPERATOR.getName()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("DELETE returns 403 for non-admin (OPERATOR) role")
        void deleteWithOperatorReturns403() throws Exception {
            mockMvc.perform(delete(ROLES_PATH + "/{id}", ROLE_ID)
                            .with(user("operator").roles(OPERATOR.getName())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET is allowed for ADMIN role")
        void getWithAdminIsAllowed() throws Exception {
            when(roleService.getRoleById(ROLE_ID)).thenReturn(role);

            mockMvc.perform(get(ROLES_PATH + "/{id}", ROLE_ID)
                            .with(user("admin").roles(ADMIN.getName())))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Bean validation")
    class Validation {

        @Test
        @DisplayName("POST returns 400 with field error when name is blank")
        void createBlankNameReturns400() throws Exception {
            var invalid = new CreateRoleRequest("  ");

            mockMvc.perform(post(ROLES_PATH)
                            .with(user("admin").roles(ADMIN.getName()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Content-Type", "application/problem+json"))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors.name").exists());
        }

        @Test
        @DisplayName("PUT returns 400 when name exceeds max length")
        void updateTooLongNameReturns400() throws Exception {
            String tooLongName = "x".repeat(51);
            var invalid = new UpdateRoleRequest(tooLongName);

            mockMvc.perform(put(ROLES_PATH + "/{id}", ROLE_ID)
                            .with(user("admin").roles(ADMIN.getName()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.name").exists());
        }
    }

    @Nested
    @DisplayName("Exception mapping")
    class ExceptionMapping {

        @Test
        @DisplayName("GET by id maps EntityNotFoundException to 404 ProblemDetail")
        void getByIdNotFoundReturns404() throws Exception {
            when(roleService.getRoleById(ROLE_ID_NOT_FOUND))
                    .thenThrow(new EntityNotFoundException("Role not found with id: " + ROLE_ID_NOT_FOUND));

            mockMvc.perform(get(ROLES_PATH + "/{id}", ROLE_ID_NOT_FOUND)
                            .with(user("admin").roles(ADMIN.getName())))
                    .andExpect(status().isNotFound())
                    .andExpect(header().string("Content-Type", "application/problem+json"))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.title").value("Resource not found"))
                    .andExpect(jsonPath("$.detail").value("Role not found with id: " + ROLE_ID_NOT_FOUND))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("DELETE returns 204 No Content on success (no response body)")
        void deleteReturns204() throws Exception {
            mockMvc.perform(delete(ROLES_PATH + "/{id}", ROLE_ID)
                            .with(user("admin").roles(ADMIN.getName())))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(roleService).deleteRole(ROLE_ID);
        }

        @Test
        @DisplayName("DELETE maps EntityNotFoundException to 404 ProblemDetail")
        void deleteNotFoundReturns404() throws Exception {
            doThrow(new EntityNotFoundException("Role not found with ID: " + ROLE_ID_NOT_FOUND))
                    .when(roleService).deleteRole(ROLE_ID_NOT_FOUND);

            mockMvc.perform(delete(ROLES_PATH + "/{id}", ROLE_ID_NOT_FOUND)
                            .with(user("admin").roles(ADMIN.getName())))
                    .andExpect(status().isNotFound())
                    .andExpect(header().string("Content-Type", "application/problem+json"))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.title").value("Resource not found"))
                    .andExpect(jsonPath("$.detail").value("Role not found with ID: " + ROLE_ID_NOT_FOUND))
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    @DisplayName("JSON response shape & headers")
    class ResponseShape {

        @Test
        @DisplayName("POST returns 201 with Location header, JSON content type and mapped body")
        void createReturns201WithLocationAndBody() throws Exception {
            when(roleService.createRole(any(CreateRoleRequest.class))).thenReturn(role);

            mockMvc.perform(post(ROLES_PATH)
                            .with(user("admin").roles(ADMIN.getName()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", ROLES_PATH + "/" + ROLE_ID))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(ROLE_ID))
                    .andExpect(jsonPath("$.name").value(ROLE_NAME));
        }

        @Test
        @DisplayName("GET by id returns 200 with mapped body")
        void getByIdReturnsMappedBody() throws Exception {
            when(roleService.getRoleById(ROLE_ID)).thenReturn(role);

            mockMvc.perform(get(ROLES_PATH + "/{id}", ROLE_ID)
                            .with(user("admin").roles(ADMIN.getName())))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(ROLE_ID))
                    .andExpect(jsonPath("$.name").value(ROLE_NAME));
        }

        @Test
        @DisplayName("GET (list) returns 200 with a paginated JSON body (content + page metadata)")
        void getRolesReturnsPagedJson() throws Exception {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Role> page = new PageImpl<>(List.of(role), pageable, 1);
            when(roleService.getRoles(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get(ROLES_PATH)
                            .with(user("admin").roles(ADMIN.getName())))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(ROLE_ID))
                    .andExpect(jsonPath("$.content[0].name").value(ROLE_NAME))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.size").value(20));
        }

        @Test
        @DisplayName("GET (list) returns 200 with an empty content array when there are no roles")
        void getRolesReturnsEmptyPagedJson() throws Exception {
            Pageable pageable = PageRequest.of(0, 20);
            when(roleService.getRoles(any(Pageable.class))).thenReturn(Page.empty(pageable));

            mockMvc.perform(get(ROLES_PATH)
                            .with(user("admin").roles(ADMIN.getName())))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }
}

