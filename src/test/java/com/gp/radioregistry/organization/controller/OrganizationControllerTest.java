package com.gp.radioregistry.organization.controller;

import com.gp.radioregistry.organization.domain.Organization;
import com.gp.radioregistry.organization.dto.request.CreateOrganizationRequest;
import com.gp.radioregistry.organization.dto.request.UpdateOrganizationRequest;
import com.gp.radioregistry.organization.dto.response.OrganizationResponse;
import com.gp.radioregistry.organization.service.OrganizationService;
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

import static com.gp.radioregistry.constant.ApiConstants.ORGANIZATIONS_PATH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrganizationController unit tests")
class OrganizationControllerTest {

    private static final Long ORGANIZATION_ID = 1L;
    private static final Long ORGANIZATION_ID_NOT_FOUND = 99L;
    private static final String ORGANIZATION_NAME = "San Gabriel hospital group";
    private static final String ORGANIZATION_CODE = "SAN-GAB-H";
    private static final String ORGANIZATION_DESCRIPTION = "Main headquarters";

    private static final String ORGANIZATION_NAME_UPDATE = "San Philip hospital group";
    private static final String ORGANIZATION_CODE_UPDATE = "SAN-PHI-H";
    private static final String ORGANIZATION_DESCRIPTION_UPDATE = "Main headquarters";

    @Mock
    private OrganizationService organizationService;

    @InjectMocks
    private OrganizationController organizationController;

    private Organization organization;

    @BeforeEach
    void setUp() {
        organization = Organization.builder()
                .id(ORGANIZATION_ID)
                .name(ORGANIZATION_NAME)
                .code(ORGANIZATION_CODE)
                .description(ORGANIZATION_DESCRIPTION)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("createOrganization")
    class CreateOrganization {

        @Test
        @DisplayName("should return 201 Created with location header and mapped body")
        void createOrganization_returnsCreated() {
            var request = new CreateOrganizationRequest(ORGANIZATION_NAME, ORGANIZATION_CODE, ORGANIZATION_DESCRIPTION);
            when(organizationService.createOrganization(request)).thenReturn(organization);

            ResponseEntity<OrganizationResponse> response = organizationController.createOrganization(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(URI.create(ORGANIZATIONS_PATH + "/" + ORGANIZATION_ID), response.getHeaders().getLocation());
            assertNotNull(response.getBody());
            assertEquals(ORGANIZATION_ID, response.getBody().id());
            assertEquals(ORGANIZATION_NAME, response.getBody().name());
            assertEquals(ORGANIZATION_CODE, response.getBody().code());
            assertEquals(ORGANIZATION_DESCRIPTION, response.getBody().description());
            assertNotNull(response.getBody().departments());
            assertNotNull(response.getBody().devices());
            verify(organizationService).createOrganization(request);
        }
    }

    @Nested
    @DisplayName("updateOrganization")
    class UpdateOrganization {

        @Test
        @DisplayName("should return 200 OK with the updated organization")
        void updateOrganization_returnsOk() {
            var request = new UpdateOrganizationRequest(ORGANIZATION_NAME_UPDATE, ORGANIZATION_CODE_UPDATE, ORGANIZATION_DESCRIPTION_UPDATE);
            organization.setName(ORGANIZATION_NAME_UPDATE);
            organization.setCode(ORGANIZATION_CODE_UPDATE);
            organization.setDescription(ORGANIZATION_DESCRIPTION_UPDATE);
            when(organizationService.updateOrganization(ORGANIZATION_ID, request)).thenReturn(organization);

            ResponseEntity<OrganizationResponse> response =
                    organizationController.updateOrganization(ORGANIZATION_ID, request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(ORGANIZATION_NAME_UPDATE, response.getBody().name());
            assertEquals(ORGANIZATION_CODE_UPDATE, response.getBody().code());
            assertEquals(ORGANIZATION_DESCRIPTION_UPDATE, response.getBody().description());
            verify(organizationService).updateOrganization(ORGANIZATION_ID, request);
        }

        @Test
        @DisplayName("should propagate EntityNotFoundException when organization does not exist")
        void updateOrganization_notFound() {
            var request = new UpdateOrganizationRequest(ORGANIZATION_NAME_UPDATE, ORGANIZATION_CODE_UPDATE, ORGANIZATION_DESCRIPTION_UPDATE);
            when(organizationService.updateOrganization(anyLong(), any(UpdateOrganizationRequest.class)))
                    .thenThrow(new EntityNotFoundException("Organization not found"));

            assertThrows(EntityNotFoundException.class,
                    () -> organizationController.updateOrganization(ORGANIZATION_ID_NOT_FOUND, request));
        }
    }

    @Nested
    @DisplayName("deleteOrganization")
    class DeleteOrganization {

        @Test
        @DisplayName("should return 204 No Content and delegate to service")
        void deleteOrganization_returnsNoContent() {
            ResponseEntity<Void> response = organizationController.deleteOrganization(ORGANIZATION_ID);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertNull(response.getBody());
            verify(organizationService).deleteOrganization(ORGANIZATION_ID);
        }

        @Test
        @DisplayName("should propagate EntityNotFoundException when organization does not exist")
        void deleteOrganization_notFound() {
            doThrow(new EntityNotFoundException("Organization not found"))
                    .when(organizationService).deleteOrganization(ORGANIZATION_ID_NOT_FOUND);

            assertThrows(EntityNotFoundException.class, () -> organizationController.deleteOrganization(ORGANIZATION_ID_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("getOrganizationTreeById")
    class GetOrganizationTreeById {

        @Test
        @DisplayName("should return 200 OK with the mapped organization tree")
        void getOrganizationTreeById_returnsOk() {
            when(organizationService.getOrganizationById(ORGANIZATION_ID)).thenReturn(organization);

            ResponseEntity<OrganizationResponse> response =
                    organizationController.getOrganizationTreeById(ORGANIZATION_ID);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(ORGANIZATION_ID, response.getBody().id());
            assertEquals(ORGANIZATION_NAME, response.getBody().name());
            assertNotNull(response.getBody().departments());
            assertNotNull(response.getBody().devices());
            verify(organizationService).getOrganizationById(ORGANIZATION_ID);
        }

        @Test
        @DisplayName("should propagate EntityNotFoundException when organization does not exist")
        void getOrganizationTreeById_notFound() {
            when(organizationService.getOrganizationById(ORGANIZATION_ID_NOT_FOUND))
                    .thenThrow(new EntityNotFoundException("Organization not found"));

            assertThrows(EntityNotFoundException.class,
                    () -> organizationController.getOrganizationTreeById(ORGANIZATION_ID_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("getOrganizations")
    class GetOrganizations {

        @Test
        @DisplayName("should return 200 OK with a mapped page of organizations")
        void getOrganizations_returnsPage() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Organization> page = new PageImpl<>(List.of(organization), pageable, 1);
            when(organizationService.getOrganizations(pageable)).thenReturn(page);

            ResponseEntity<Page<OrganizationResponse>> response =
                    organizationController.getOrganizations(pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getTotalElements());
            assertEquals(ORGANIZATION_ID, response.getBody().getContent().getFirst().id());
            verify(organizationService).getOrganizations(pageable);
        }

        @Test
        @DisplayName("should return 200 OK with an empty page when no organizations exist")
        void getOrganizations_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 20);
            when(organizationService.getOrganizations(pageable)).thenReturn(Page.empty(pageable));

            ResponseEntity<Page<OrganizationResponse>> response =
                    organizationController.getOrganizations(pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());
        }
    }
}

