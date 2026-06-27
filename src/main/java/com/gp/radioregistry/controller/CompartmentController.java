package com.gp.radioregistry.controller;


import com.gp.radioregistry.request.CreateCompartmentRequest;
import com.gp.radioregistry.response.CompartmentResponse;
import com.gp.radioregistry.service.CompartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/compartments")
@Tag(name = "Compartment controller", description = "API for managing compartments")
public class CompartmentController {
    private final CompartmentService compartmentService;

    public CompartmentController(CompartmentService compartmentService) {
        this.compartmentService = compartmentService;
    }

    @PostMapping
    @Operation(summary = "Create a new compartment", description = "Receives a new compartment, validates it and saves it.")
    public ResponseEntity<CompartmentResponse> createCompartment(@Valid @RequestBody CreateCompartmentRequest request) {
        log.info("Creation request received for compartment with name: {}", request.name());

        var compartment = compartmentService.createCompartment(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(CompartmentResponse.fromEntity(compartment));
    }
}

