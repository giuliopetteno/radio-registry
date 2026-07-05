package com.gp.radioregistry.organization.service;

import com.gp.radioregistry.organization.domain.Organization;
import com.gp.radioregistry.organization.dto.request.CreateOrganizationRequest;
import com.gp.radioregistry.organization.dto.request.UpdateOrganizationRequest;
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
@DisplayName("OrganizationService unit tests")
class OrganizationServiceTest {

    private static final Long ORGANIZATION_ID = 1L;
    private static final Long ORGANIZATION_ID_NOT_FOUND = 99L;
    private static final String ORGANIZATION_NAME = "San Gabriel hospital group";
    private static final String ORGANIZATION_CODE = "SAN-GAB-H";
    private static final String ORGANIZATION_DESCRIPTION = "Main headquarters";

    private static final String ORGANIZATION_NAME_UPDATE = "San Philip hospital group";
    private static final String ORGANIZATION_CODE_UPDATE = "SAN-PHI-H";
    private static final String ORGANIZATION_DESCRIPTION_UPDATE = "Main headquarters";

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private OrganizationService organizationService;

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
        @DisplayName("should build and save an organization from the request")
        void createOrganization_savesOrganization() {
            var request = new CreateOrganizationRequest(ORGANIZATION_NAME, ORGANIZATION_CODE, ORGANIZATION_DESCRIPTION);
            when(organizationRepository.save(any(Organization.class))).thenReturn(organization);

            Organization result = organizationService.createOrganization(request);

            assertSame(organization, result);
            ArgumentCaptor<Organization> captor = ArgumentCaptor.forClass(Organization.class);
            verify(organizationRepository).save(captor.capture());
            Organization saved = captor.getValue();
            assertEquals(ORGANIZATION_NAME, saved.getName());
            assertEquals(ORGANIZATION_CODE, saved.getCode());
            assertEquals(ORGANIZATION_DESCRIPTION, saved.getDescription());
        }
    }

    @Nested
    @DisplayName("updateOrganization")
    class UpdateOrganization {

        @Test
        @DisplayName("should update fields when present and save")
        void updateOrganization_updatesAndSaves() {
            var request = new UpdateOrganizationRequest(ORGANIZATION_NAME_UPDATE, ORGANIZATION_CODE_UPDATE, ORGANIZATION_DESCRIPTION_UPDATE);
            when(organizationRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.of(organization));
            when(organizationRepository.save(organization)).thenReturn(organization);

            Organization result = organizationService.updateOrganization(ORGANIZATION_ID, request);

            assertEquals(ORGANIZATION_NAME_UPDATE, result.getName());
            assertEquals(ORGANIZATION_CODE_UPDATE, result.getCode());
            assertEquals(ORGANIZATION_DESCRIPTION_UPDATE, result.getDescription());
            verify(organizationRepository).save(organization);
        }

        @Test
        @DisplayName("should keep existing name/code when request values are null")
        void updateOrganization_keepsExistingWhenNull() {
            var request = new UpdateOrganizationRequest(null, null, null);
            when(organizationRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.of(organization));
            when(organizationRepository.save(organization)).thenReturn(organization);

            Organization result = organizationService.updateOrganization(ORGANIZATION_ID, request);

            assertEquals(ORGANIZATION_NAME, result.getName());
            assertEquals(ORGANIZATION_CODE, result.getCode());
            assertNull(result.getDescription());
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when organization does not exist")
        void updateOrganization_notFound() {
            var request = new UpdateOrganizationRequest(ORGANIZATION_NAME_UPDATE, null, null);
            when(organizationRepository.findById(ORGANIZATION_ID_NOT_FOUND)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> organizationService.updateOrganization(ORGANIZATION_ID_NOT_FOUND, request));
            verify(organizationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteOrganization")
    class DeleteOrganization {

        @Test
        @DisplayName("should delete the organization when it exists")
        void deleteOrganization_deletes() {
            when(organizationRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.of(organization));

            organizationService.deleteOrganization(ORGANIZATION_ID);

            verify(organizationRepository).delete(organization);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when organization does not exist")
        void deleteOrganization_notFound() {
            when(organizationRepository.findById(ORGANIZATION_ID_NOT_FOUND)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> organizationService.deleteOrganization(ORGANIZATION_ID_NOT_FOUND));
            verify(organizationRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("getOrganizations")
    class GetOrganizations {

        @Test
        @DisplayName("should return the page returned by the repository")
        void getOrganizations_returnsPage() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Organization> page = new PageImpl<>(List.of(organization), pageable, 1);
            when(organizationRepository.findAll(pageable)).thenReturn(page);

            Page<Organization> result = organizationService.getOrganizations(pageable);

            assertEquals(1, result.getTotalElements());
            assertSame(organization, result.getContent().getFirst());
        }
    }

    @Nested
    @DisplayName("getOrganizationById")
    class GetOrganizationById {

        @Test
        @DisplayName("should return the organization when it exists")
        void getOrganizationById_returnsOrganization() {
            when(organizationRepository.findById(ORGANIZATION_ID)).thenReturn(Optional.of(organization));

            Organization result = organizationService.getOrganizationById(ORGANIZATION_ID);

            assertSame(organization, result);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when organization does not exist")
        void getOrganizationById_notFound() {
            when(organizationRepository.findById(ORGANIZATION_ID_NOT_FOUND)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> organizationService.getOrganizationById(ORGANIZATION_ID_NOT_FOUND));
        }
    }
}

