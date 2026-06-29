package com.gp.radioregistry.controller;


import com.gp.radioregistry.request.CreateCompartmentRequest;
import com.gp.radioregistry.request.UpdateCompartmentRequest;
import com.gp.radioregistry.response.CompartmentResponse;
import com.gp.radioregistry.service.CompartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static com.gp.radioregistry.constant.AppConstants.Api.COMPARTMENTS_PATH;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping(COMPARTMENTS_PATH)
@Tag(name = "Compartments controller", description = "API for managing compartments")
public class CompartmentController {
    private final CompartmentService compartmentService;

    @PostMapping
    @Operation(summary = "Create a new compartment", description = "Receives a new compartment, validates it and saves it.")
    public ResponseEntity<CompartmentResponse> createCompartment(@Valid @RequestBody CreateCompartmentRequest request) {
        log.info("Creation request received for compartment with name: {}", request.name());

        var compartment = compartmentService.createCompartment(request);

        return ResponseEntity.created(URI.create(String.format("%s/%d", COMPARTMENTS_PATH, compartment.getId()))).body(CompartmentResponse.fromEntity(compartment));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update request for compartment", description = "Updates a compartment.")
    public ResponseEntity<CompartmentResponse> updateCompartment(@PathVariable Long id, @Valid @RequestBody UpdateCompartmentRequest request) {
        log.info("Update request received for compartment with id: {}", id);

        var compartment = compartmentService.updateCompartment(id, request);

        return ResponseEntity.ok(CompartmentResponse.fromEntity(compartment));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete request for compartment", description = "Deletes a compartment by ID.")
    public ResponseEntity<Void> deleteCompartment(@PathVariable Long id) {
        log.info("Delete request received for compartment with id: {}", id);

        compartmentService.deleteCompartment(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "List all compartments", description = "Returns the complete list of compartments available in the system.")
    public ResponseEntity<List<CompartmentResponse>> getCompartments() {
        log.info("Request received to fetch all compartments");

        var compartments = compartmentService.getCompartments();

        return ResponseEntity.ok(compartments.stream().map(CompartmentResponse::fromEntity).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get compartment by id", description = "Returns a single compartment matching the given id.")
    public ResponseEntity<CompartmentResponse> getCompartmentById(@PathVariable Long id) {
        log.info("Request received to fetch compartment with id: {}", id);

        var compartment = compartmentService.getCompartmentById(id);

        return ResponseEntity.ok(CompartmentResponse.fromEntity(compartment));
    }
}

