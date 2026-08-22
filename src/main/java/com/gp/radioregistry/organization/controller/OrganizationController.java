package com.gp.radioregistry.organization.controller;

import com.gp.radioregistry.organization.dto.request.CreateOrganizationRequest;
import com.gp.radioregistry.organization.dto.request.UpdateOrganizationRequest;
import com.gp.radioregistry.organization.dto.response.OrganizationTreeResponse;
import com.gp.radioregistry.organization.dto.response.OrganizationSummaryResponse;
import com.gp.radioregistry.organization.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static com.gp.radioregistry.constant.ApiConstants.ORGANIZATIONS_PATH;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ORGANIZATIONS_PATH)
@Tag(name = "Organizations controller", description = "API for managing organizations")
public class OrganizationController {
    private final OrganizationService organizationService;

    @PostMapping
    @Operation(summary = "Create a new organization", description = "Receives a new organization, validates it and saves it.")
    public ResponseEntity<OrganizationSummaryResponse> createOrganization(@Valid @RequestBody CreateOrganizationRequest request) {
        log.info("Creation request received for organization with name: {}", request.name());

        var organization = organizationService.createOrganization(request);

        return ResponseEntity.created(URI.create(String.format("%s/%d", ORGANIZATIONS_PATH, organization.getId()))).body(OrganizationSummaryResponse.fromEntity(organization));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update request for organization", description = "Updates an organization.")
    public ResponseEntity<OrganizationSummaryResponse> updateOrganization(@PathVariable Long id, @Valid @RequestBody UpdateOrganizationRequest request) {
        log.info("Update request received for organization with id: {}", id);

        var organization = organizationService.updateOrganization(id, request);

        return ResponseEntity.ok(OrganizationSummaryResponse.fromEntity(organization));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete request for organization", description = "Deletes an organization by ID.")
    public ResponseEntity<Void> deleteOrganization(@PathVariable Long id) {
        log.info("Delete request received for organization with id: {}", id);

        organizationService.deleteOrganization(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/tree")
    @Operation(summary = "Request organization tree", description = "Retrieves the entire tree of an organization")
    public ResponseEntity<OrganizationTreeResponse> getOrganizationTreeById(@PathVariable Long id) {
        log.info("Request received for organization tree with id: {}", id);

        var organization = organizationService.getOrganizationById(id);

        return ResponseEntity.ok(OrganizationTreeResponse.fromEntity(organization));
    }

    @GetMapping
    @Operation(summary = "List all organizations", description = "Returns the complete list of organizations available in the system.")
    public ResponseEntity<Page<OrganizationSummaryResponse>> getOrganizations(@ParameterObject Pageable pageable) {
        log.info("Request received to fetch all organizations");

        var organizations = organizationService.getOrganizations(pageable);

        return ResponseEntity.ok(organizations.map(OrganizationSummaryResponse::fromEntity));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organization by id", description = "Returns a single organization matching the given id.")
    public ResponseEntity<OrganizationSummaryResponse> getOrganizationById(@PathVariable Long id) {
        log.info("Request received to fetch organization with id: {}", id);

        var organization = organizationService.getOrganizationById(id);

        return ResponseEntity.ok(OrganizationSummaryResponse.fromEntity(organization));
    }
}

