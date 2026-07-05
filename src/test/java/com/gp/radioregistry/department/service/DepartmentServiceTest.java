package com.gp.radioregistry.department.service;

import com.gp.radioregistry.department.domain.Department;
import com.gp.radioregistry.department.dto.request.CreateDepartmentRequest;
import com.gp.radioregistry.department.dto.request.UpdateDepartmentRequest;
import com.gp.radioregistry.department.repository.DepartmentRepository;
import com.gp.radioregistry.organization.domain.Organization;
import com.gp.radioregistry.organization.repository.OrganizationRepository;
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
@DisplayName("DepartmentService unit tests")
class DepartmentServiceTest {

    private static final Long DEPARTMENT_ID = 1L;
    private static final Long DEPARTMENT_ID_NOT_FOUND = 99L;
    private static final Long ORGANIZATION_ID = 10L;
    private static final Long PARENT_DEPARTMENT_ID = 5L;
    private static final String DEPARTMENT_NAME = "Radiology";
    private static final String DEPARTMENT_CODE = "RAD-1";
    private static final String DEPARTMENT_DESCRIPTION = "Radiology principal department";

    private static final String DEPARTMENT_NAME_UPDATE = "Radiology 2";
    private static final String DEPARTMENT_CODE_UPDATE = "RAD-2";
    private static final String DEPARTMENT_DESCRIPTION_UPDATE = "Radiology secondary department";

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private Department department;

    @BeforeEach
    void setUp() {
        department = Department.builder()
                .id(DEPARTMENT_ID)
                .name(DEPARTMENT_NAME)
                .code(DEPARTMENT_CODE)
                .description(DEPARTMENT_DESCRIPTION)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("createDepartment")
    class CreateDepartment {

        @Test
        @DisplayName("should resolve organization reference, build and save the department")
        void createDepartment_withOrganization() {
            var request = new CreateDepartmentRequest(
                    DEPARTMENT_NAME, DEPARTMENT_CODE, DEPARTMENT_DESCRIPTION, ORGANIZATION_ID, null);
            var orgRef = new Organization();
            when(organizationRepository.getReferenceById(ORGANIZATION_ID)).thenReturn(orgRef);
            when(departmentRepository.save(any(Department.class))).thenReturn(department);

            Department result = departmentService.createDepartment(request);

            assertSame(department, result);
            ArgumentCaptor<Department> captor = ArgumentCaptor.forClass(Department.class);
            verify(departmentRepository).save(captor.capture());
            Department saved = captor.getValue();
            assertEquals(DEPARTMENT_NAME, saved.getName());
            assertEquals(DEPARTMENT_CODE, saved.getCode());
            assertEquals(DEPARTMENT_DESCRIPTION, saved.getDescription());
            assertSame(orgRef, saved.getOrganization());
            assertNull(saved.getParentDepartment());
            verify(departmentRepository, never()).getReferenceById(any());
        }

        @Test
        @DisplayName("should resolve parent department reference when parentDepartmentId is provided")
        void createDepartment_withParentDepartment() {
            var request = new CreateDepartmentRequest(
                        DEPARTMENT_NAME_UPDATE, "RAD-2", "Radiology secondary department", null, PARENT_DEPARTMENT_ID);
            var parentRef = new Department();
            when(departmentRepository.getReferenceById(PARENT_DEPARTMENT_ID)).thenReturn(parentRef);
            when(departmentRepository.save(any(Department.class))).thenReturn(department);

            departmentService.createDepartment(request);

            ArgumentCaptor<Department> captor = ArgumentCaptor.forClass(Department.class);
            verify(departmentRepository).save(captor.capture());
            assertSame(parentRef, captor.getValue().getParentDepartment());
            assertNull(captor.getValue().getOrganization());
            verify(organizationRepository, never()).getReferenceById(any());
        }
    }

    @Nested
    @DisplayName("updateDepartment")
    class UpdateDepartment {

        @Test
        @DisplayName("should update fields, resolve references and save")
        void updateDepartment_updatesAndSaves() {
            var request = new UpdateDepartmentRequest(
                DEPARTMENT_NAME_UPDATE, DEPARTMENT_CODE_UPDATE, DEPARTMENT_DESCRIPTION_UPDATE, ORGANIZATION_ID, null);
            var orgRef = new Organization();
            when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.of(department));
            when(organizationRepository.getReferenceById(ORGANIZATION_ID)).thenReturn(orgRef);
            when(departmentRepository.save(department)).thenReturn(department);

            Department result = departmentService.updateDepartment(DEPARTMENT_ID, request);

            assertEquals(DEPARTMENT_NAME_UPDATE, result.getName());
            assertEquals(DEPARTMENT_CODE_UPDATE, result.getCode());
            assertEquals(DEPARTMENT_DESCRIPTION_UPDATE, result.getDescription());
            assertSame(orgRef, result.getOrganization());
            assertNull(result.getParentDepartment());
            verify(departmentRepository).save(department);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when department does not exist")
        void updateDepartment_notFound() {
            var request = new UpdateDepartmentRequest(
                    DEPARTMENT_NAME_UPDATE, DEPARTMENT_CODE_UPDATE, DEPARTMENT_DESCRIPTION_UPDATE, ORGANIZATION_ID, null);
            when(departmentRepository.findById(DEPARTMENT_ID_NOT_FOUND)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> departmentService.updateDepartment(DEPARTMENT_ID_NOT_FOUND, request));
            verify(departmentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteDepartment")
    class DeleteDepartment {

        @Test
        @DisplayName("should delete the department when it exists")
        void deleteDepartment_deletes() {
            when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.of(department));

            departmentService.deleteDepartment(DEPARTMENT_ID);

            verify(departmentRepository).delete(department);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when department does not exist")
        void deleteDepartment_notFound() {
            when(departmentRepository.findById(DEPARTMENT_ID_NOT_FOUND)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> departmentService.deleteDepartment(DEPARTMENT_ID_NOT_FOUND));
            verify(departmentRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("getDepartments")
    class GetDepartments {

        @Test
        @DisplayName("should return the page returned by the repository")
        void getDepartments_returnsPage() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Department> page = new PageImpl<>(List.of(department), pageable, 1);
            when(departmentRepository.findAll(pageable)).thenReturn(page);

            Page<Department> result = departmentService.getDepartments(pageable);

            assertEquals(1, result.getTotalElements());
            assertSame(department, result.getContent().getFirst());
        }
    }

    @Nested
    @DisplayName("getDepartmentById")
    class GetDepartmentById {

        @Test
        @DisplayName("should return the department when it exists")
        void getDepartmentById_returns() {
            when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.of(department));

            Department result = departmentService.getDepartmentById(DEPARTMENT_ID);

            assertSame(department, result);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when department does not exist")
        void getDepartmentById_notFound() {
            when(departmentRepository.findById(DEPARTMENT_ID_NOT_FOUND)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> departmentService.getDepartmentById(DEPARTMENT_ID_NOT_FOUND));
        }
    }
}

