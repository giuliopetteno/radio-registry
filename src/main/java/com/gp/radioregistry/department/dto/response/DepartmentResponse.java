package com.gp.radioregistry.department.dto.response;

import com.gp.radioregistry.department.domain.Department;
import com.gp.radioregistry.device.dto.response.DeviceResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

public record DepartmentResponse(
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

    @Schema(description = "Child departments of this department")
    List<DepartmentResponse> childDepartments,

    @Schema(description = "Devices related to the department")
    List<DeviceResponse> devices,

    @Schema(description = "Record creation date and time")
    OffsetDateTime createdAt,

    @Schema(description = "Record update date and time")
    OffsetDateTime updatedAt
) {
    public static DepartmentResponse fromEntity(Department department) {
        if (department == null) {
            return null;
        }

        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getCode(),
                department.getDescription(),
                department.getOrganization() != null ? department.getOrganization().getId() : null,
                department.getParentDepartment() != null ? department.getParentDepartment().getId() : null,
                department.getChildDepartments().stream().map(DepartmentResponse::fromEntity).toList(),
                department.getDevices().stream().map(DeviceResponse::fromEntity).toList(),
                department.getCreatedAt(),
                department.getUpdatedAt()
        );
    }
}

