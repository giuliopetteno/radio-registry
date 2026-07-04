package com.gp.radioregistry.organization.service;

import com.gp.radioregistry.audit.annotation.Auditable;
import com.gp.radioregistry.audit.enums.AuditAction;
import com.gp.radioregistry.audit.enums.AuditEntityType;
import com.gp.radioregistry.organization.domain.Organization;
import com.gp.radioregistry.organization.dto.request.CreateOrganizationRequest;
import com.gp.radioregistry.organization.dto.request.UpdateOrganizationRequest;
import com.gp.radioregistry.organization.repository.OrganizationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    private final OrganizationRepository organizationRepository;

    @Auditable(action = AuditAction.CREATE, entityType = AuditEntityType.ORGANIZATION, entityId = "#result.id")
    public Organization createOrganization(CreateOrganizationRequest request) {
        var organization = Organization.builder()
                .name(request.name())
                .code(request.code())
                .description(request.description())
                .build();

        return organizationRepository.save(organization);
    }

    @Auditable(action = AuditAction.UPDATE, entityType = AuditEntityType.ORGANIZATION, entityId = "#id")
    public Organization updateOrganization(Long id, UpdateOrganizationRequest request) {
        var organization = getOrganizationById(id);
        Optional.ofNullable(request.name()).ifPresent(organization::setName);
        Optional.ofNullable(request.code()).ifPresent(organization::setCode);
        organization.setDescription(request.description());

        return organizationRepository.save(organization);
    }

    @Auditable(action = AuditAction.DELETE, entityType = AuditEntityType.ORGANIZATION, entityId = "#id")
    public void deleteOrganization(Long id) {
        var organization = organizationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Organization not found with id: " + id));
        organizationRepository.delete(organization);
    }

    public Page<Organization> getOrganizations(Pageable pageable) {
        return organizationRepository.findAll(pageable);
    }

    public Organization getOrganizationById(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found with ID: " + id));
    }
}

