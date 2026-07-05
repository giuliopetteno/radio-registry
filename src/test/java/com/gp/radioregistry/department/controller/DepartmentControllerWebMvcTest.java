package com.gp.radioregistry.department.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gp.radioregistry.department.domain.Department;
import com.gp.radioregistry.department.dto.request.CreateDepartmentRequest;
import com.gp.radioregistry.department.dto.request.UpdateDepartmentRequest;
import com.gp.radioregistry.department.service.DepartmentService;
import com.gp.radioregistry.device.domain.Device;
import com.gp.radioregistry.devicetype.domain.DeviceType;
import com.gp.radioregistry.organization.domain.Organization;
import com.gp.radioregistry.security.config.SecurityConfig;
import com.gp.radioregistry.security.enums.Role;
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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static com.gp.radioregistry.constant.ApiConstants.DEPARTMENTS_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentController.class)
@Import(SecurityConfig.class)
@DisplayName("DepartmentController @WebMvcTest")
class DepartmentControllerWebMvcTest {

    private static final Long DEPARTMENT_ID = 1L;
    private static final Long DEPARTMENT_ID_NOT_FOUND = 99L;
    private static final Long ORGANIZATION_ID = 10L;
    private static final Long CHILD_DEPARTMENT_ID = 2L;
    private static final Long DEVICE_ID = 5L;
    private static final Long DEVICE_TYPE_ID = 7L;

    private static final String DEPARTMENT_NAME = "Radiology";
    private static final String DEPARTMENT_CODE = "RAD-1";
    private static final String DEPARTMENT_DESCRIPTION = "Radiology principal department";

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private DepartmentService departmentService;

    private Department department;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();

        OffsetDateTime now = OffsetDateTime.now();

        Organization organization = Organization.builder().id(ORGANIZATION_ID).build();

        DeviceType deviceType = DeviceType.builder().id(DEVICE_TYPE_ID).build();
        Device device = Device.builder()
                .id(DEVICE_ID)
                .name("CT Scanner")
                .deviceType(deviceType)
                .serialNumber("SN-123")
                .installationDate(LocalDate.of(2024, 1, 15))
                .createdAt(now)
                .updatedAt(now)
                .build();

        Department childDepartment = Department.builder()
                .id(CHILD_DEPARTMENT_ID)
                .name("Pediatric Radiology")
                .code("RAD-1-C")
                .organization(organization)
                .createdAt(now)
                .updatedAt(now)
                .build();

        department = Department.builder()
                .id(DEPARTMENT_ID)
                .name(DEPARTMENT_NAME)
                .code(DEPARTMENT_CODE)
                .description(DEPARTMENT_DESCRIPTION)
                .organization(organization)
                .childDepartments(List.of(childDepartment))
                .devices(List.of(device))
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private CreateDepartmentRequest validCreateRequest() {
        return new CreateDepartmentRequest(DEPARTMENT_NAME, DEPARTMENT_CODE, DEPARTMENT_DESCRIPTION, ORGANIZATION_ID, null);
    }

    @Nested
    @DisplayName("Security")
    class Security {

        @Test
        @DisplayName("GET returns 401 when unauthenticated")
        void getUnauthenticatedReturns401() throws Exception {
            mockMvc.perform(get(DEPARTMENTS_PATH))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST returns 401 when unauthenticated")
        void createUnauthenticatedReturns401() throws Exception {
            mockMvc.perform(post(DEPARTMENTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest())))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST returns 403 for OPERATOR (read-only) role")
        void createWithOperatorReturns403() throws Exception {
            mockMvc.perform(post(DEPARTMENTS_PATH)
                            .with(user("operator").roles(Role.OPERATOR.getName()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("PUT returns 401 when unauthenticated")
        void updateUnauthenticatedReturns401() throws Exception {
            var request = new UpdateDepartmentRequest(DEPARTMENT_NAME, DEPARTMENT_CODE, DEPARTMENT_DESCRIPTION, ORGANIZATION_ID, null);

            mockMvc.perform(put(DEPARTMENTS_PATH + "/{id}", DEPARTMENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("PUT returns 403 for OPERATOR (read-only) role")
        void updateWithOperatorReturns403() throws Exception {
            var request = new UpdateDepartmentRequest(DEPARTMENT_NAME, DEPARTMENT_CODE, DEPARTMENT_DESCRIPTION, ORGANIZATION_ID, null);

            mockMvc.perform(put(DEPARTMENTS_PATH + "/{id}", DEPARTMENT_ID)
                            .with(user("operator").roles(Role.OPERATOR.getName()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("DELETE returns 401 when unauthenticated")
        void deleteUnauthenticatedReturns401() throws Exception {
            mockMvc.perform(delete(DEPARTMENTS_PATH + "/{id}", DEPARTMENT_ID))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("DELETE returns 403 for OPERATOR (read-only) role")
        void deleteWithOperatorReturns403() throws Exception {
            mockMvc.perform(delete(DEPARTMENTS_PATH + "/{id}", DEPARTMENT_ID)
                            .with(user("operator").roles(Role.OPERATOR.getName())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET is allowed for OPERATOR role")
        void getWithOperatorIsAllowed() throws Exception {
            when(departmentService.getDepartmentById(DEPARTMENT_ID)).thenReturn(department);

            mockMvc.perform(get(DEPARTMENTS_PATH + "/{id}", DEPARTMENT_ID)
                            .with(user("operator").roles(Role.OPERATOR.getName())))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Bean validation")
    class Validation {

        @Test
        @DisplayName("POST returns 400 with field errors when name is blank and code is blank")
        void createBlankFieldsReturns400() throws Exception {
            var invalid = new CreateDepartmentRequest("  ", "", DEPARTMENT_DESCRIPTION, ORGANIZATION_ID, null);

            mockMvc.perform(post(DEPARTMENTS_PATH)
                            .with(user("tech").roles(Role.TECHNICIAN.getName()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Content-Type", "application/problem+json"))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors.name").exists())
                    .andExpect(jsonPath("$.errors.code").exists());
        }

        @Test
        @DisplayName("POST returns 400 when neither organizationId nor parentDepartmentId is set (@AssertTrue)")
        void createViolatesXorReturns400() throws Exception {
            var invalid = new CreateDepartmentRequest(DEPARTMENT_NAME, DEPARTMENT_CODE, DEPARTMENT_DESCRIPTION, null, null);

            mockMvc.perform(post(DEPARTMENTS_PATH)
                            .with(user("tech").roles(Role.TECHNICIAN.getName()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").exists());
        }

        @Test
        @DisplayName("PUT returns 400 when name exceeds max length")
        void updateTooLongNameReturns400() throws Exception {
            String tooLongName = "x".repeat(51);
            var invalid = new UpdateDepartmentRequest(tooLongName, DEPARTMENT_CODE, DEPARTMENT_DESCRIPTION, ORGANIZATION_ID, null);

            mockMvc.perform(put(DEPARTMENTS_PATH + "/{id}", DEPARTMENT_ID)
                            .with(user("tech").roles(Role.TECHNICIAN.getName()))
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
            when(departmentService.getDepartmentById(DEPARTMENT_ID_NOT_FOUND))
                    .thenThrow(new EntityNotFoundException("Department not found with id: " + DEPARTMENT_ID_NOT_FOUND));

            mockMvc.perform(get(DEPARTMENTS_PATH + "/{id}", DEPARTMENT_ID_NOT_FOUND)
                            .with(user("operator").roles(Role.OPERATOR.getName())))
                    .andExpect(status().isNotFound())
                    .andExpect(header().string("Content-Type", "application/problem+json"))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.title").value("Resource not found"))
                    .andExpect(jsonPath("$.detail").value("Department not found with id: " + DEPARTMENT_ID_NOT_FOUND))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("DELETE returns 204 No Content on success (no response body)")
        void deleteReturns204() throws Exception {
            mockMvc.perform(delete(DEPARTMENTS_PATH + "/{id}", DEPARTMENT_ID)
                            .with(user("tech").roles(Role.TECHNICIAN.getName())))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(departmentService).deleteDepartment(DEPARTMENT_ID);
        }

        @Test
        @DisplayName("DELETE maps EntityNotFoundException to 404 ProblemDetail")
        void deleteNotFoundReturns404() throws Exception {
            doThrow(new EntityNotFoundException("Department not found with ID: " + DEPARTMENT_ID_NOT_FOUND))
                    .when(departmentService).deleteDepartment(DEPARTMENT_ID_NOT_FOUND);

            mockMvc.perform(delete(DEPARTMENTS_PATH + "/{id}", DEPARTMENT_ID_NOT_FOUND)
                            .with(user("tech").roles(Role.TECHNICIAN.getName())))
                    .andExpect(status().isNotFound())
                    .andExpect(header().string("Content-Type", "application/problem+json"))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.title").value("Resource not found"))
                    .andExpect(jsonPath("$.detail").value("Department not found with ID: " + DEPARTMENT_ID_NOT_FOUND))
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    @DisplayName("JSON response shape & headers")
    class ResponseShape {

        @Test
        @DisplayName("POST returns 201 with Location header, JSON content type and mapped body")
        void createReturns201WithLocationAndBody() throws Exception {
            when(departmentService.createDepartment(any(CreateDepartmentRequest.class))).thenReturn(department);

            mockMvc.perform(post(DEPARTMENTS_PATH)
                            .with(user("tech").roles(Role.TECHNICIAN.getName()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", DEPARTMENTS_PATH + "/" + DEPARTMENT_ID))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(DEPARTMENT_ID))
                    .andExpect(jsonPath("$.name").value(DEPARTMENT_NAME))
                    .andExpect(jsonPath("$.code").value(DEPARTMENT_CODE));
        }

        @Test
        @DisplayName("GET by id exposes nested childDepartments and devices collections")
        void getByIdExposesNestedCollections() throws Exception {
            when(departmentService.getDepartmentById(DEPARTMENT_ID)).thenReturn(department);

            mockMvc.perform(get(DEPARTMENTS_PATH + "/{id}", DEPARTMENT_ID)
                            .with(user("operator").roles(Role.OPERATOR.getName())))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(DEPARTMENT_ID))
                    .andExpect(jsonPath("$.organizationId").value(ORGANIZATION_ID))
                    .andExpect(jsonPath("$.childDepartments").isArray())
                    .andExpect(jsonPath("$.childDepartments[0].id").value(CHILD_DEPARTMENT_ID))
                    .andExpect(jsonPath("$.childDepartments[0].name").value("Pediatric Radiology"))
                    .andExpect(jsonPath("$.devices").isArray())
                    .andExpect(jsonPath("$.devices[0].id").value(DEVICE_ID))
                    .andExpect(jsonPath("$.devices[0].deviceTypeId").value(DEVICE_TYPE_ID))
                    .andExpect(jsonPath("$.devices[0].serialNumber").value("SN-123"));
        }

        @Test
        @DisplayName("GET (list) returns 200 with a paginated JSON body (content + page metadata)")
        void getDepartmentsReturnsPagedJson() throws Exception {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Department> page = new PageImpl<>(List.of(department), pageable, 1);
            when(departmentService.getDepartments(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get(DEPARTMENTS_PATH)
                            .with(user("operator").roles(Role.OPERATOR.getName())))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(DEPARTMENT_ID))
                    .andExpect(jsonPath("$.content[0].name").value(DEPARTMENT_NAME))
                    .andExpect(jsonPath("$.content[0].organizationId").value(ORGANIZATION_ID))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.size").value(20));
        }

        @Test
        @DisplayName("GET (list) returns 200 with an empty content array when there are no departments")
        void getDepartmentsReturnsEmptyPagedJson() throws Exception {
            Pageable pageable = PageRequest.of(0, 20);
            when(departmentService.getDepartments(any(Pageable.class))).thenReturn(Page.empty(pageable));

            mockMvc.perform(get(DEPARTMENTS_PATH)
                            .with(user("operator").roles(Role.OPERATOR.getName())))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }
}
