package com.gp.radioregistry.department.dto.response;

import com.gp.radioregistry.department.domain.Department;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

public record DepartmentSummaryResponse(
    @Schema(description = "Unique department ID")
    Long id,

    @Schema(description = "Department name")
    String name,

    @Schema(description = "Unique identification code")
    String code,

    @Schema(description = "Department description")
    String description,

    @Schema(description = "Organization to which the department belongs")
    Long organizationId,

    @Schema(description = "Parent department (if exists) to which this child department belongs")
    Long parentDepartmentId,

    @Schema(description = "Record creation date and time")
    OffsetDateTime createdAt,

    @Schema(description = "Record update date and time")
    OffsetDateTime updatedAt
) {
    public static DepartmentSummaryResponse fromEntity(Department department) {
        if (department == null) {
            return null;
        }

        return new DepartmentSummaryResponse(
                department.getId(),
                department.getName(),
                department.getCode(),
                department.getDescription(),
                department.getOrganization() != null ? department.getOrganization().getId() : null,
                department.getParentDepartment() != null ? department.getParentDepartment().getId() : null,
                department.getCreatedAt(),
                department.getUpdatedAt()
        );
    }
}

