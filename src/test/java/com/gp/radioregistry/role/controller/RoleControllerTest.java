package com.gp.radioregistry.role.controller;

import com.gp.radioregistry.role.domain.Role;
import com.gp.radioregistry.role.dto.request.CreateRoleRequest;
import com.gp.radioregistry.role.dto.request.UpdateRoleRequest;
import com.gp.radioregistry.role.dto.response.RoleResponse;
import com.gp.radioregistry.role.service.RoleService;
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

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

import static com.gp.radioregistry.constant.ApiConstants.ROLES_PATH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleController unit tests")
class RoleControllerTest {

    private static final Long ROLE_ID = 1L;
    private static final Long ROLE_ID_NOT_FOUND = 99L;
    private static final String ROLE_NAME = "ADMIN";

    private static final String ROLE_NAME_UPDATE = "OPERATOR";

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleController roleController;

    private Role role;

    @BeforeEach
    void setUp() {
        role = Role.builder()
                .id(ROLE_ID)
                .name(ROLE_NAME)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("createRole")
    class CreateRole {

        @Test
        @DisplayName("should return 201 Created with location header and mapped body")
        void createRole_returnsCreated() {
            var request = new CreateRoleRequest(ROLE_NAME);
            when(roleService.createRole(request)).thenReturn(role);

            ResponseEntity<RoleResponse> response = roleController.createRole(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(URI.create(ROLES_PATH + "/" + ROLE_ID), response.getHeaders().getLocation());
            assertNotNull(response.getBody());
            assertEquals(ROLE_ID, response.getBody().id());
            assertEquals(ROLE_NAME, response.getBody().name());
            verify(roleService).createRole(request);
        }
    }

    @Nested
    @DisplayName("updateRole")
    class UpdateRole {

        @Test
        @DisplayName("should return 200 OK with the updated role")
        void updateRole_returnsOk() {
            var request = new UpdateRoleRequest(ROLE_NAME_UPDATE);
            role.setName(ROLE_NAME_UPDATE);
            when(roleService.updateRole(ROLE_ID, request)).thenReturn(role);

            ResponseEntity<RoleResponse> response = roleController.updateRole(ROLE_ID, request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(ROLE_NAME_UPDATE, response.getBody().name());
            verify(roleService).updateRole(ROLE_ID, request);
        }

        @Test
        @DisplayName("should propagate EntityNotFoundException when role does not exist")
        void updateRole_notFound() {
            var request = new UpdateRoleRequest(ROLE_NAME_UPDATE);
            when(roleService.updateRole(anyLong(), any(UpdateRoleRequest.class)))
                    .thenThrow(new EntityNotFoundException("Role not found"));

            assertThrows(EntityNotFoundException.class, () -> roleController.updateRole(ROLE_ID_NOT_FOUND, request));
        }
    }

    @Nested
    @DisplayName("deleteRole")
    class DeleteRole {

        @Test
        @DisplayName("should return 204 No Content and delegate to service")
        void deleteRole_returnsNoContent() {
            ResponseEntity<Void> response = roleController.deleteRole(ROLE_ID);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertNull(response.getBody());
            verify(roleService).deleteRole(ROLE_ID);
        }

        @Test
        @DisplayName("should propagate EntityNotFoundException when role does not exist")
        void deleteRole_notFound() {
            doThrow(new EntityNotFoundException("Role not found")).when(roleService).deleteRole(ROLE_ID_NOT_FOUND);

            assertThrows(EntityNotFoundException.class, () -> roleController.deleteRole(ROLE_ID_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("getRoles")
    class GetRoles {

        @Test
        @DisplayName("should return 200 OK with a mapped page of roles")
        void getRoles_returnsPage() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Role> page = new PageImpl<>(List.of(role), pageable, 1);
            when(roleService.getRoles(pageable)).thenReturn(page);

            ResponseEntity<Page<RoleResponse>> response = roleController.getRoles(pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getTotalElements());
            assertEquals(ROLE_ID, response.getBody().getContent().getFirst().id());
            verify(roleService).getRoles(pageable);
        }

        @Test
        @DisplayName("should return 200 OK with an empty page when no roles exist")
        void getRoles_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 20);
            when(roleService.getRoles(pageable)).thenReturn(Page.empty(pageable));

            ResponseEntity<Page<RoleResponse>> response = roleController.getRoles(pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());
        }
    }

    @Nested
    @DisplayName("getRoleById")
    class GetRoleById {

        @Test
        @DisplayName("should return 200 OK with the mapped role")
        void getRoleById_returnsOk() {
            when(roleService.getRoleById(ROLE_ID)).thenReturn(role);

            ResponseEntity<RoleResponse> response = roleController.getRoleById(ROLE_ID);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(ROLE_ID, response.getBody().id());
            assertEquals(ROLE_NAME, response.getBody().name());
            verify(roleService).getRoleById(ROLE_ID);
        }

        @Test
        @DisplayName("should propagate EntityNotFoundException when role does not exist")
        void getRoleById_notFound() {
            when(roleService.getRoleById(ROLE_ID_NOT_FOUND)).thenThrow(new EntityNotFoundException("Role not found"));

            assertThrows(EntityNotFoundException.class, () -> roleController.getRoleById(ROLE_ID_NOT_FOUND));
        }
    }
}

