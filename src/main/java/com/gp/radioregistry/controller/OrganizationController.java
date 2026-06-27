package com.gp.radioregistry.controller;

import com.gp.radioregistry.request.CreateOrganizationRequest;
import com.gp.radioregistry.response.OrganizationResponse;
import com.gp.radioregistry.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequestMapping("/organizations")
@Tag(name = "Organizations controller", description = "API for managing organizations")
public class OrganizationController {
    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping("/{id}/tree")
    @Operation(summary = "Request organization tree", description = "Retrieves the entire tree of an organization")
    public ResponseEntity<OrganizationResponse> getOrganizationTreeById(@PathVariable Long id) {
        log.info("Request received for organization tree with id: {}", id);

        var organization = organizationService.getOrganizationById(id);

        return ResponseEntity.ok(OrganizationResponse.fromEntity(organization));
    }

    @PostMapping
    @Operation(summary = "Create a new organization", description = "Receives a new organization, validates it and saves it.")
    public ResponseEntity<OrganizationResponse> createOrganization(@Valid @RequestBody CreateOrganizationRequest request) {
        log.info("Creation request received for organization with name: {}", request.name());

        var organization = organizationService.createOrganization(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(OrganizationResponse.fromEntity(organization));
    }

}

