package com.gp.radioregistry.department.service;

import com.gp.radioregistry.audit.annotation.Auditable;
import com.gp.radioregistry.audit.enums.AuditAction;
import com.gp.radioregistry.audit.enums.AuditEntityType;
import com.gp.radioregistry.department.domain.Department;
import com.gp.radioregistry.department.dto.request.CreateDepartmentRequest;
import com.gp.radioregistry.department.dto.request.UpdateDepartmentRequest;
import com.gp.radioregistry.department.repository.DepartmentRepository;
import com.gp.radioregistry.organization.repository.OrganizationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final OrganizationRepository organizationRepository;

    @Auditable(action = AuditAction.CREATE, entityType = AuditEntityType.DEPARTMENT, entityId = "#result.id")
    public Department createDepartment(CreateDepartmentRequest request) {
        var department = Department.builder()
                .name(request.name())
                .code(request.code())
                .description(request.description())
                .organization(request.organizationId() != null ? organizationRepository.getReferenceById(request.organizationId()) : null)
                .parentDepartment(request.parentDepartmentId() != null ? departmentRepository.getReferenceById(request.parentDepartmentId()) : null)
                .build();

        return departmentRepository.save(department);
    }

    @Auditable(action = AuditAction.UPDATE, entityType = AuditEntityType.DEPARTMENT, entityId = "#id")
    public Department updateDepartment(Long id, UpdateDepartmentRequest request) {
        var department = getDepartmentById(id);
        Optional.ofNullable(request.name()).ifPresent(department::setName);
        Optional.ofNullable(request.code()).ifPresent(department::setCode);
        department.setDescription(request.description());
        department.setOrganization(request.organizationId() != null ? organizationRepository.getReferenceById(request.organizationId()) : null);
        department.setParentDepartment(request.parentDepartmentId() != null ? departmentRepository.getReferenceById(request.parentDepartmentId()) : null);

        return departmentRepository.save(department);
    }

    @Auditable(action = AuditAction.DELETE, entityType = AuditEntityType.DEPARTMENT, entityId = "#id")
    public void deleteDepartment(Long id) {
        var department = departmentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Department not found with id: " + id));
        departmentRepository.delete(department);
    }

    public List<Department> getDepartments() {
        return departmentRepository.findAll();
    }

    public Department getDepartmentById(Long id) {

        return departmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found with ID: " + id));
    }
}

