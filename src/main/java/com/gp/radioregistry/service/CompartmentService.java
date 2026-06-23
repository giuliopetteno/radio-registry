package com.gp.radioregistry.service;

import com.gp.radioregistry.domain.Compartment;
import com.gp.radioregistry.repository.CompartmentRepository;
import com.gp.radioregistry.repository.OrganizationRepository;
import com.gp.radioregistry.request.CreateCompartmentRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompartmentService {
    private final CompartmentRepository compartmentRepository;
    private final OrganizationRepository organizationRepository;

    public CompartmentService(CompartmentRepository compartmentRepository, OrganizationRepository organizationRepository) {
        this.compartmentRepository = compartmentRepository;
        this.organizationRepository = organizationRepository;
    }

    public Compartment createCompartment(CreateCompartmentRequest request) {
        Compartment compartment = Compartment.builder()
                .name(request.name())
                .code(request.code())
                .description(request.description())
                .organization(request.organizationId() != null ? organizationRepository.getReferenceById(request.organizationId()) : null)
                .parentCompartment(request.parentCompartmentId() != null ? compartmentRepository.getReferenceById(request.parentCompartmentId()) : null)
                .build();

        return compartmentRepository.save(compartment);
    }

    public List<Compartment> getCompartments() {
        return compartmentRepository.findAll();
    }

    public Compartment getCompartmentById(Long id) {

        return compartmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Compartment not found with ID: " + id));
    }
}

