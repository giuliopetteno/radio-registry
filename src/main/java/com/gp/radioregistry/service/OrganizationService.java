package com.gp.radioregistry.service;

import com.gp.radioregistry.domain.Organization;
import com.gp.radioregistry.repository.OrganizationRepository;
import com.gp.radioregistry.request.CreateOrganizationRequest;
import com.gp.radioregistry.request.UpdateOrganizationRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    private final OrganizationRepository organizationRepository;

    public Organization createOrganization(CreateOrganizationRequest request) {
        var organization = Organization.builder()
                .name(request.name())
                .code(request.code())
                .description(request.description())
                .build();

        return organizationRepository.save(organization);
    }

    public Organization updateOrganization(Long id, UpdateOrganizationRequest request) {
        var organization = getOrganizationById(id);
        Optional.ofNullable(request.name()).ifPresent(organization::setName);
        Optional.ofNullable(request.code()).ifPresent(organization::setCode);
        organization.setDescription(request.description());

        return organizationRepository.save(organization);
    }

    public void deleteOrganization(Long id) {
        var organization = organizationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Organization not found with id: " + id));
        organizationRepository.delete(organization);
    }

    public List<Organization> getOrganizations() {

        return organizationRepository.findAll();
    }

    public Organization getOrganizationById(Long id) {

        return organizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found with ID: " + id));
    }
}

