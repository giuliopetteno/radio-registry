package com.gp.radioregistry.department.controller;

import com.gp.radioregistry.department.domain.Department;
import com.gp.radioregistry.department.dto.request.CreateDepartmentRequest;
import com.gp.radioregistry.department.dto.request.UpdateDepartmentRequest;
import com.gp.radioregistry.department.dto.response.DepartmentResponse;
import com.gp.radioregistry.department.service.DepartmentService;
import com.gp.radioregistry.organization.domain.Organization;
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

import static com.gp.radioregistry.constant.ApiConstants.DEPARTMENTS_PATH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentController unit tests")
class DepartmentControllerTest {

    private static final Long DEPARTMENT_ID = 1L;
    private static final Long DEPARTMENT_ID_NOT_FOUND = 99L;
    private static final Long ORGANIZATION_ID = 10L;
    private static final String DEPARTMENT_NAME = "Radiology";
    private static final String DEPARTMENT_CODE = "RAD-1";
    private static final String DEPARTMENT_DESCRIPTION = "Radiology principal department";

    private static final String DEPARTMENT_NAME_UPDATE = "Radiology 2";
    private static final String DEPARTMENT_CODE_UPDATE = "RAD-2";
    private static final String DEPARTMENT_DESCRIPTION_UPDATE = "Radiology secondary department";

    @Mock
    private DepartmentService departmentService;

    @Mock
    private Organization organization;

    @InjectMocks
    private DepartmentController departmentController;

    private Department department;

    @BeforeEach
    void setUp() {
        department = Department.builder()
                .id(DEPARTMENT_ID)
                .name(DEPARTMENT_NAME)
                .code(DEPARTMENT_CODE)
                .description(DEPARTMENT_DESCRIPTION)
                .organization(organization)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("createDepartment")
    class CreateDepartment {

        @Test
        @DisplayName("should return 201 Created with location header and mapped body")
        void createDepartment_returnsCreated() {
            var request = new CreateDepartmentRequest(
                    DEPARTMENT_NAME, DEPARTMENT_CODE, DEPARTMENT_DESCRIPTION, ORGANIZATION_ID, null);
            when(organization.getId()).thenReturn(ORGANIZATION_ID);
            when(departmentService.createDepartment(request)).thenReturn(department);

            ResponseEntity<DepartmentResponse> response = departmentController.createDepartment(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(URI.create(DEPARTMENTS_PATH + "/" + DEPARTMENT_ID), response.getHeaders().getLocation());
            var body = response.getBody();
            assertNotNull(body);
            assertEquals(DEPARTMENT_ID, body.id());
            assertEquals(DEPARTMENT_NAME, body.name());
            assertEquals(DEPARTMENT_CODE, body.code());
            assertEquals(DEPARTMENT_DESCRIPTION, body.description());
            assertEquals(ORGANIZATION_ID, body.organizationId());
            assertNull(body.parentDepartmentId());
            verify(departmentService).createDepartment(request);
        }

        @Test
        @DisplayName("should propagate exceptions thrown by the service")
        void createDepartment_propagatesServiceException() {
            var request = new CreateDepartmentRequest(
                    DEPARTMENT_NAME, DEPARTMENT_CODE, DEPARTMENT_DESCRIPTION, ORGANIZATION_ID, null);
            when(departmentService.createDepartment(request)).thenThrow(new RuntimeException("error from service"));

            assertThrows(RuntimeException.class, () -> departmentController.createDepartment(request));
        }
    }

    @Nested
    @DisplayName("updateDepartment")
    class UpdateDepartment {

        @Test
        @DisplayName("should return 200 OK with the updated department")
        void updateDepartment_returnsOk() {
            var request = new UpdateDepartmentRequest(
                    DEPARTMENT_NAME_UPDATE, DEPARTMENT_CODE_UPDATE, DEPARTMENT_DESCRIPTION_UPDATE, ORGANIZATION_ID, null);
            department.setName(request.name());
            department.setCode(request.code());
            department.setDescription(request.description());
            when(departmentService.updateDepartment(DEPARTMENT_ID, request)).thenReturn(department);

            ResponseEntity<DepartmentResponse> response = departmentController.updateDepartment(DEPARTMENT_ID, request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            var body = response.getBody();
            assertNotNull(body);
            assertEquals(DEPARTMENT_ID, body.id());
            assertEquals(DEPARTMENT_NAME_UPDATE, body.name());
            assertEquals(DEPARTMENT_CODE_UPDATE, body.code());
            assertEquals(DEPARTMENT_DESCRIPTION_UPDATE, body.description());
            verify(departmentService).updateDepartment(DEPARTMENT_ID, request);
        }

        @Test
        @DisplayName("should propagate EntityNotFoundException when department does not exist")
        void updateDepartment_notFound() {
            var request = new UpdateDepartmentRequest(
                    DEPARTMENT_NAME_UPDATE, DEPARTMENT_CODE_UPDATE, DEPARTMENT_DESCRIPTION_UPDATE, ORGANIZATION_ID, null);
            when(departmentService.updateDepartment(anyLong(), any(UpdateDepartmentRequest.class)))
                    .thenThrow(new EntityNotFoundException("Department not found with ID: " + DEPARTMENT_ID_NOT_FOUND));

            assertThrows(EntityNotFoundException.class,
                    () -> departmentController.updateDepartment(DEPARTMENT_ID_NOT_FOUND, request));
        }
    }

    @Nested
    @DisplayName("deleteDepartment")
    class DeleteDepartment {

        @Test
        @DisplayName("should return 204 No Content and delegate to the service")
        void deleteDepartment_returnsNoContent() {
            ResponseEntity<Void> response = departmentController.deleteDepartment(DEPARTMENT_ID);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertNull(response.getBody());
            verify(departmentService).deleteDepartment(DEPARTMENT_ID);
        }

        @Test
        @DisplayName("should propagate EntityNotFoundException when department does not exist")
        void deleteDepartment_notFound() {
            doThrow(new EntityNotFoundException("Department not found with ID: " + DEPARTMENT_ID_NOT_FOUND))
                    .when(departmentService).deleteDepartment(DEPARTMENT_ID_NOT_FOUND);

            assertThrows(EntityNotFoundException.class, () -> departmentController.deleteDepartment(DEPARTMENT_ID_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("getDepartments")
    class GetDepartments {

        @Test
        @DisplayName("should return 200 OK with a mapped page of departments")
        void getDepartments_returnsPage() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Department> page = new PageImpl<>(List.of(department), pageable, 1);
            when(departmentService.getDepartments(pageable)).thenReturn(page);

            ResponseEntity<Page<DepartmentResponse>> response = departmentController.getDepartments(pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            var body = response.getBody();
            assertNotNull(body);
            assertEquals(1, body.getTotalElements());
            assertEquals(DEPARTMENT_ID, body.getContent().getFirst().id());
            assertEquals(DEPARTMENT_NAME, body.getContent().getFirst().name());
            verify(departmentService).getDepartments(pageable);
        }

        @Test
        @DisplayName("should return 200 OK with an empty page when no departments exist")
        void getDepartments_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 20);
            when(departmentService.getDepartments(pageable)).thenReturn(Page.empty(pageable));

            ResponseEntity<Page<DepartmentResponse>> response = departmentController.getDepartments(pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            var body = response.getBody();
            assertNotNull(body);
            assertTrue(body.isEmpty());
        }
    }

    @Nested
    @DisplayName("getDepartmentById")
    class GetDepartmentById {

        @Test
        @DisplayName("should return 200 OK with the mapped department")
        void getDepartmentById_returnsOk() {
            when(organization.getId()).thenReturn(ORGANIZATION_ID);
            when(departmentService.getDepartmentById(DEPARTMENT_ID)).thenReturn(department);

            ResponseEntity<DepartmentResponse> response = departmentController.getDepartmentById(DEPARTMENT_ID);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            var body = response.getBody();
            assertNotNull(body);
            assertEquals(DEPARTMENT_ID, body.id());
            assertEquals(DEPARTMENT_NAME, body.name());
            assertEquals(DEPARTMENT_CODE, body.code());
            assertEquals(ORGANIZATION_ID, body.organizationId());
            assertNotNull(body.childDepartments());
            assertNotNull(body.devices());
            verify(departmentService).getDepartmentById(DEPARTMENT_ID);
        }

        @Test
        @DisplayName("should propagate EntityNotFoundException when department does not exist")
        void getDepartmentById_notFound() {
            when(departmentService.getDepartmentById(DEPARTMENT_ID_NOT_FOUND))
                    .thenThrow(new EntityNotFoundException("Department not found with ID: " + DEPARTMENT_ID_NOT_FOUND));

            assertThrows(EntityNotFoundException.class, () -> departmentController.getDepartmentById(DEPARTMENT_ID_NOT_FOUND));
        }
    }
}

