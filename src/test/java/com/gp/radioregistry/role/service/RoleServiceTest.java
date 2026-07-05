package com.gp.radioregistry.role.service;

import com.gp.radioregistry.exception.ResourceAlreadyExistsException;
import com.gp.radioregistry.role.domain.Role;
import com.gp.radioregistry.role.dto.request.CreateRoleRequest;
import com.gp.radioregistry.role.dto.request.UpdateRoleRequest;
import com.gp.radioregistry.role.repository.RoleRepository;
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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleService unit tests")
class RoleServiceTest {

    private static final Long ROLE_ID = 1L;
    private static final Long ROLE_ID_NOT_FOUND = 99L;
    private static final String ROLE_NAME = "ADMIN";

    private static final String ROLE_NAME_UPDATE = "OPERATOR";

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

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
        @DisplayName("should normalize the name to trimmed upper-case and save")
        void createRole_savesNormalizedName() {
            var request = new CreateRoleRequest("  " + ROLE_NAME + "  ");
            when(roleRepository.existsByName(ROLE_NAME.toUpperCase())).thenReturn(false);
            when(roleRepository.save(any(Role.class))).thenReturn(role);

            Role result = roleService.createRole(request);

            assertSame(role, result);
            ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
            verify(roleRepository).save(captor.capture());
            assertEquals(ROLE_NAME.toUpperCase(), captor.getValue().getName());
        }

        @Test
        @DisplayName("should throw ResourceAlreadyExistsException when name already exists")
        void createRole_alreadyExists() {
            var request = new CreateRoleRequest("admin");
            when(roleRepository.existsByName(ROLE_NAME.toUpperCase())).thenReturn(true);

            assertThrows(ResourceAlreadyExistsException.class, () -> roleService.createRole(request));
            verify(roleRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateRole")
    class UpdateRole {

        @Test
        @DisplayName("should update the name when present and save")
        void updateRole_updatesAndSaves() {
            var request = new UpdateRoleRequest(ROLE_NAME_UPDATE);
            when(roleRepository.findById(ROLE_ID)).thenReturn(Optional.of(role));
            when(roleRepository.save(role)).thenReturn(role);

            Role result = roleService.updateRole(ROLE_ID, request);

            assertEquals(ROLE_NAME_UPDATE, result.getName());
            verify(roleRepository).save(role);
        }

        @Test
        @DisplayName("should keep existing name when request name is null")
        void updateRole_keepsNameWhenNull() {
            var request = new UpdateRoleRequest(null);
            when(roleRepository.findById(ROLE_ID)).thenReturn(Optional.of(role));
            when(roleRepository.save(role)).thenReturn(role);

            Role result = roleService.updateRole(ROLE_ID, request);

            assertEquals(ROLE_NAME, result.getName());
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when role does not exist")
        void updateRole_notFound() {
            var request = new UpdateRoleRequest(ROLE_NAME_UPDATE);
            when(roleRepository.findById(ROLE_ID_NOT_FOUND)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> roleService.updateRole(ROLE_ID_NOT_FOUND, request));
            verify(roleRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteRole")
    class DeleteRole {

        @Test
        @DisplayName("should delete the role when it exists")
        void deleteRole_deletes() {
            when(roleRepository.findById(ROLE_ID)).thenReturn(Optional.of(role));

            roleService.deleteRole(ROLE_ID);

            verify(roleRepository).delete(role);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when role does not exist")
        void deleteRole_notFound() {
            when(roleRepository.findById(ROLE_ID_NOT_FOUND)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> roleService.deleteRole(ROLE_ID_NOT_FOUND));
            verify(roleRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("getRoles")
    class GetRoles {

        @Test
        @DisplayName("should return the page returned by the repository")
        void getRoles_returnsPage() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Role> page = new PageImpl<>(List.of(role), pageable, 1);
            when(roleRepository.findAll(pageable)).thenReturn(page);

            Page<Role> result = roleService.getRoles(pageable);

            assertEquals(1, result.getTotalElements());
            assertSame(role, result.getContent().getFirst());
        }
    }

    @Nested
    @DisplayName("getRoleById")
    class GetRoleById {

        @Test
        @DisplayName("should return the role when it exists")
        void getRoleById_returns() {
            when(roleRepository.findById(ROLE_ID)).thenReturn(Optional.of(role));

            Role result = roleService.getRoleById(ROLE_ID);

            assertSame(role, result);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when role does not exist")
        void getRoleById_notFound() {
            when(roleRepository.findById(ROLE_ID_NOT_FOUND)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> roleService.getRoleById(ROLE_ID_NOT_FOUND));
        }
    }
}

