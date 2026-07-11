package com.gp.radioregistry.organization.controller;

import com.gp.radioregistry.department.domain.Department;
import com.gp.radioregistry.device.domain.Device;
import com.gp.radioregistry.devicetype.domain.DeviceType;
import com.gp.radioregistry.organization.domain.Organization;
import com.gp.radioregistry.organization.dto.request.CreateOrganizationRequest;
import com.gp.radioregistry.organization.dto.request.UpdateOrganizationRequest;
import com.gp.radioregistry.organization.service.OrganizationService;
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
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static com.gp.radioregistry.constant.ApiConstants.ORGANIZATIONS_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrganizationController.class)
@Import(SecurityConfig.class)
@DisplayName("OrganizationController @WebMvcTest")
class OrganizationControllerWebMvcTest {

    private static final Long ORGANIZATION_ID = 10L;
    private static final Long ORGANIZATION_ID_NOT_FOUND = 99L;
    private static final Long DEPARTMENT_ID = 1L;
    private static final Long DEVICE_ID = 5L;
    private static final Long DEVICE_TYPE_ID = 7L;

    private static final String ORGANIZATION_NAME = "General Hospital";
    private static final String ORGANIZATION_CODE = "GH-1";
    private static final String ORGANIZATION_DESCRIPTION = "Main hospital organization";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private OrganizationService organizationService;

    private Organization organization;

    @BeforeEach
    void setUp() {
        OffsetDateTime now = OffsetDateTime.now();

        Department department = Department.builder()
                .id(DEPARTMENT_ID)
                .name("Radiology")
                .code("RAD-1")
                .createdAt(now)
                .updatedAt(now)
                .build();

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

        organization = Organization.builder()
                .id(ORGANIZATION_ID)
                .name(ORGANIZATION_NAME)
                .code(ORGANIZATION_CODE)
                .description(ORGANIZATION_DESCRIPTION)
                .departments(List.of(department))
                .devices(List.of(device))
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private CreateOrganizationRequest validCreateRequest() {
        return new CreateOrganizationRequest(ORGANIZATION_NAME, ORGANIZATION_CODE, ORGANIZATION_DESCRIPTION);
    }

    @Nested
    @DisplayName("Security")
    class Security {

        @Test
        @DisplayName("GET returns 401 when unauthenticated")
        void getUnauthenticatedReturns401() throws Exception {
            mockMvc.perform(get(ORGANIZATIONS_PATH))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST returns 401 when unauthenticated")
        void createUnauthenticatedReturns401() throws Exception {
            mockMvc.perform(post(ORGANIZATIONS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(validCreateRequest())))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST returns 403 for OPERATOR (read-only) role")
        void createWithOperatorReturns403() throws Exception {
            mockMvc.perform(post(ORGANIZATIONS_PATH)
                            .with(user("operator").roles(Role.OPERATOR.getName()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(validCreateRequest())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("PUT returns 401 when unauthenticated")
        void updateUnauthenticatedReturns401() throws Exception {
            var request = new UpdateOrganizationRequest(ORGANIZATION_NAME, ORGANIZATION_CODE, ORGANIZATION_DESCRIPTION);

            mockMvc.perform(put(ORGANIZATIONS_PATH + "/{id}", ORGANIZATION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("PUT returns 403 for OPERATOR (read-only) role")
        void updateWithOperatorReturns403() throws Exception {
            var request = new UpdateOrganizationRequest(ORGANIZATION_NAME, ORGANIZATION_CODE, ORGANIZATION_DESCRIPTION);

            mockMvc.perform(put(ORGANIZATIONS_PATH + "/{id}", ORGANIZATION_ID)
                            .with(user("operator").roles(Role.OPERATOR.getName()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("DELETE returns 401 when unauthenticated")
        void deleteUnauthenticatedReturns401() throws Exception {
            mockMvc.perform(delete(ORGANIZATIONS_PATH + "/{id}", ORGANIZATION_ID))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("DELETE returns 403 for OPERATOR (read-only) role")
        void deleteWithOperatorReturns403() throws Exception {
            mockMvc.perform(delete(ORGANIZATIONS_PATH + "/{id}", ORGANIZATION_ID)
                            .with(user("operator").roles(Role.OPERATOR.getName())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET tree is allowed for OPERATOR role")
        void getTreeWithOperatorIsAllowed() throws Exception {
            when(organizationService.getOrganizationById(ORGANIZATION_ID)).thenReturn(organization);

            mockMvc.perform(get(ORGANIZATIONS_PATH + "/{id}/tree", ORGANIZATION_ID)
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
            var invalid = new CreateOrganizationRequest("  ", "", ORGANIZATION_DESCRIPTION);

            mockMvc.perform(post(ORGANIZATIONS_PATH)
                            .with(user("tech").roles(Role.TECHNICIAN.getName()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Content-Type", "application/problem+json"))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors.name").exists())
                    .andExpect(jsonPath("$.errors.code").exists());
        }

        @Test
        @DisplayName("PUT returns 400 when name exceeds max length")
        void updateTooLongNameReturns400() throws Exception {
            String tooLongName = "x".repeat(51);
            var invalid = new UpdateOrganizationRequest(tooLongName, ORGANIZATION_CODE, ORGANIZATION_DESCRIPTION);

            mockMvc.perform(put(ORGANIZATIONS_PATH + "/{id}", ORGANIZATION_ID)
                            .with(user("tech").roles(Role.TECHNICIAN.getName()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.name").exists());
        }
    }

    @Nested
    @DisplayName("Exception mapping")
    class ExceptionMapping {

        @Test
        @DisplayName("GET tree by id maps EntityNotFoundException to 404 ProblemDetail")
        void getTreeByIdNotFoundReturns404() throws Exception {
            when(organizationService.getOrganizationById(ORGANIZATION_ID_NOT_FOUND))
                    .thenThrow(new EntityNotFoundException("Organization not found with id: " + ORGANIZATION_ID_NOT_FOUND));

            mockMvc.perform(get(ORGANIZATIONS_PATH + "/{id}/tree", ORGANIZATION_ID_NOT_FOUND)
                            .with(user("operator").roles(Role.OPERATOR.getName())))
                    .andExpect(status().isNotFound())
                    .andExpect(header().string("Content-Type", "application/problem+json"))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.title").value("Resource not found"))
                    .andExpect(jsonPath("$.detail").value("Organization not found with id: " + ORGANIZATION_ID_NOT_FOUND))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("DELETE returns 204 No Content on success (no response body)")
        void deleteReturns204() throws Exception {
            mockMvc.perform(delete(ORGANIZATIONS_PATH + "/{id}", ORGANIZATION_ID)
                            .with(user("tech").roles(Role.TECHNICIAN.getName())))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(organizationService).deleteOrganization(ORGANIZATION_ID);
        }

        @Test
        @DisplayName("DELETE maps EntityNotFoundException to 404 ProblemDetail")
        void deleteNotFoundReturns404() throws Exception {
            doThrow(new EntityNotFoundException("Organization not found with ID: " + ORGANIZATION_ID_NOT_FOUND))
                    .when(organizationService).deleteOrganization(ORGANIZATION_ID_NOT_FOUND);

            mockMvc.perform(delete(ORGANIZATIONS_PATH + "/{id}", ORGANIZATION_ID_NOT_FOUND)
                            .with(user("tech").roles(Role.TECHNICIAN.getName())))
                    .andExpect(status().isNotFound())
                    .andExpect(header().string("Content-Type", "application/problem+json"))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.title").value("Resource not found"))
                    .andExpect(jsonPath("$.detail").value("Organization not found with ID: " + ORGANIZATION_ID_NOT_FOUND))
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    @DisplayName("JSON response shape & headers")
    class ResponseShape {

        @Test
        @DisplayName("POST returns 201 with Location header, JSON content type and mapped body")
        void createReturns201WithLocationAndBody() throws Exception {
            when(organizationService.createOrganization(any(CreateOrganizationRequest.class))).thenReturn(organization);

            mockMvc.perform(post(ORGANIZATIONS_PATH)
                            .with(user("tech").roles(Role.TECHNICIAN.getName()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(validCreateRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", ORGANIZATIONS_PATH + "/" + ORGANIZATION_ID))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(ORGANIZATION_ID))
                    .andExpect(jsonPath("$.name").value(ORGANIZATION_NAME))
                    .andExpect(jsonPath("$.code").value(ORGANIZATION_CODE));
        }

        @Test
        @DisplayName("GET tree by id exposes nested departments and devices collections")
        void getTreeByIdExposesNestedCollections() throws Exception {
            when(organizationService.getOrganizationById(ORGANIZATION_ID)).thenReturn(organization);

            mockMvc.perform(get(ORGANIZATIONS_PATH + "/{id}/tree", ORGANIZATION_ID)
                            .with(user("operator").roles(Role.OPERATOR.getName())))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(ORGANIZATION_ID))
                    .andExpect(jsonPath("$.departments").isArray())
                    .andExpect(jsonPath("$.departments[0].id").value(DEPARTMENT_ID))
                    .andExpect(jsonPath("$.departments[0].name").value("Radiology"))
                    .andExpect(jsonPath("$.devices").isArray())
                    .andExpect(jsonPath("$.devices[0].id").value(DEVICE_ID))
                    .andExpect(jsonPath("$.devices[0].deviceTypeId").value(DEVICE_TYPE_ID))
                    .andExpect(jsonPath("$.devices[0].serialNumber").value("SN-123"));
        }

        @Test
        @DisplayName("GET (list) returns 200 with a paginated JSON body (content + page metadata)")
        void getOrganizationsReturnsPagedJson() throws Exception {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Organization> page = new PageImpl<>(List.of(organization), pageable, 1);
            when(organizationService.getOrganizations(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get(ORGANIZATIONS_PATH)
                            .with(user("operator").roles(Role.OPERATOR.getName())))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(ORGANIZATION_ID))
                    .andExpect(jsonPath("$.content[0].name").value(ORGANIZATION_NAME))
                    .andExpect(jsonPath("$.content[0].code").value(ORGANIZATION_CODE))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.size").value(20));
        }

        @Test
        @DisplayName("GET (list) returns 200 with an empty content array when there are no organizations")
        void getOrganizationsReturnsEmptyPagedJson() throws Exception {
            Pageable pageable = PageRequest.of(0, 20);
            when(organizationService.getOrganizations(any(Pageable.class))).thenReturn(Page.empty(pageable));

            mockMvc.perform(get(ORGANIZATIONS_PATH)
                            .with(user("operator").roles(Role.OPERATOR.getName())))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }
}

