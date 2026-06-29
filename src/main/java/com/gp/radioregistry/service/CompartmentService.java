package com.gp.radioregistry.service;

import com.gp.radioregistry.domain.Compartment;
import com.gp.radioregistry.repository.CompartmentRepository;
import com.gp.radioregistry.repository.OrganizationRepository;
import com.gp.radioregistry.request.CreateCompartmentRequest;
import com.gp.radioregistry.request.UpdateCompartmentRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompartmentService {
    private final CompartmentRepository compartmentRepository;
    private final OrganizationRepository organizationRepository;

    public Compartment createCompartment(CreateCompartmentRequest request) {
        var compartment = Compartment.builder()
                .name(request.name())
                .code(request.code())
                .description(request.description())
                .organization(request.organizationId() != null ? organizationRepository.getReferenceById(request.organizationId()) : null)
                .parentCompartment(request.parentCompartmentId() != null ? compartmentRepository.getReferenceById(request.parentCompartmentId()) : null)
                .build();

        return compartmentRepository.save(compartment);
    }

    public Compartment updateCompartment(Long id, UpdateCompartmentRequest request) {
        var compartment = getCompartmentById(id);
        Optional.ofNullable(request.name()).ifPresent(compartment::setName);
        Optional.ofNullable(request.code()).ifPresent(compartment::setCode);
        compartment.setDescription(request.description());
        compartment.setOrganization(request.organizationId() != null ? organizationRepository.getReferenceById(request.organizationId()) : null);
        compartment.setParentCompartment(request.parentCompartmentId() != null ? compartmentRepository.getReferenceById(request.parentCompartmentId()) : null);

        return compartmentRepository.save(compartment);
    }

    public void deleteCompartment(Long id) {
        var compartment = compartmentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Compartment not found with id: " + id));
        compartmentRepository.delete(compartment);
    }

    public List<Compartment> getCompartments() {
        return compartmentRepository.findAll();
    }

    public Compartment getCompartmentById(Long id) {

        return compartmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Compartment not found with ID: " + id));
    }
}

