package com.gp.radioregistry.department.service;

import com.gp.radioregistry.audit.annotation.Auditable;
import com.gp.radioregistry.department.domain.Department;
import com.gp.radioregistry.department.dto.request.CreateDepartmentRequest;
import com.gp.radioregistry.department.dto.request.UpdateDepartmentRequest;
import com.gp.radioregistry.department.repository.DepartmentRepository;
import com.gp.radioregistry.enums.EntityType;
import com.gp.radioregistry.enums.EventType;
import com.gp.radioregistry.exception.InvalidEntityStateException;
import com.gp.radioregistry.kafka.event.DepartmentEvent;
import com.gp.radioregistry.kafka.outboxevent.service.OutboxEventService;
import com.gp.radioregistry.organization.repository.OrganizationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final OrganizationRepository organizationRepository;
    private final OutboxEventService outboxEventService;

    @Auditable(eventType = EventType.CREATE, entityType = EntityType.DEPARTMENT, entityId = "#result.id", description = "Department creation attempt")
    public Department createDepartment(CreateDepartmentRequest request) {
        var department = Department.builder()
                .name(request.name())
                .code(request.code())
                .description(request.description())
                .organization(request.organizationId() != null ? organizationRepository.getReferenceById(request.organizationId()) : null)
                .parentDepartment(request.parentDepartmentId() != null ? departmentRepository.getReferenceById(request.parentDepartmentId()) : null)
                .build();

        var result = departmentRepository.save(department);
        outboxEventService.saveOutboxEvent(EntityType.DEPARTMENT, result.getId(), EventType.CREATE,
                                    eventId -> DepartmentEvent.of(EventType.CREATE, eventId, result));

        return result;
    }

    @Auditable(eventType = EventType.UPDATE, entityType = EntityType.DEPARTMENT, entityId = "#id", description = "Department update attempt")
    public Department updateDepartment(Long id, UpdateDepartmentRequest request) {
        if (id.equals(request.parentDepartmentId())) {
            throw new InvalidEntityStateException("A department cannot be its own parent");
        }

        if (request.parentDepartmentId() != null && departmentRepository.isCreatingCycle(id, request.parentDepartmentId())) {
            throw new InvalidEntityStateException("The assignment would create a cycle in the department hierarchy");
        }

        var department = getDepartmentById(id);
        Optional.ofNullable(request.name()).ifPresent(department::setName);
        Optional.ofNullable(request.code()).ifPresent(department::setCode);
        department.setDescription(request.description());
        department.setOrganization(request.organizationId() != null ? organizationRepository.getReferenceById(request.organizationId()) : null);
        department.setParentDepartment(request.parentDepartmentId() != null ? departmentRepository.getReferenceById(request.parentDepartmentId()) : null);

        var result = departmentRepository.save(department);
        outboxEventService.saveOutboxEvent(EntityType.DEPARTMENT, result.getId(), EventType.UPDATE,
                                    eventId -> DepartmentEvent.of(EventType.UPDATE, eventId, result));

        return result;
    }

    @Auditable(eventType = EventType.DELETE, entityType = EntityType.DEPARTMENT, entityId = "#id", description = "Department deletion attempt")
    public void deleteDepartment(Long id) {
        var department = departmentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Department not found with id: " + id));

        outboxEventService.saveOutboxEvent(EntityType.DEPARTMENT, department.getId(), EventType.DELETE,
                                    eventId -> DepartmentEvent.of(EventType.DELETE, eventId, department));
        departmentRepository.delete(department);
    }

    public Page<Department> getDepartments(Pageable pageable) {
        return departmentRepository.findAll(pageable);
    }

    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found with ID: " + id));
    }
}

