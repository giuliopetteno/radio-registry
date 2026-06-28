package com.gp.radioregistry.service;

import com.gp.radioregistry.domain.Organization;
import com.gp.radioregistry.repository.OrganizationRepository;
import com.gp.radioregistry.request.CreateOrganizationRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationService {
    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public Organization createOrganization(CreateOrganizationRequest request) {
        var organization = Organization.builder()
                .name(request.name())
                .code(request.code())
                .description(request.description())
                .build();

        return organizationRepository.save(organization);
    }

    public List<Organization> getOrganizations() {

        return organizationRepository.findAll();
    }

    public Organization getOrganizationById(Long id) {

        return organizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found with ID: " + id));
    }
}

