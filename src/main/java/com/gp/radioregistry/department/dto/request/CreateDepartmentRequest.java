package com.gp.radioregistry.department.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.gp.radioregistry.constant.ValidationConstants.*;

public record CreateDepartmentRequest(
    @Schema(description = "Department name, mandatory and cannot be empty")
    @NotBlank(message = "The department name is required")
    @Size(max = NAME_MAX_LENGTH)
    String name,

    @Schema(description = "Department code, mandatory and cannot be empty")
    @NotBlank(message = "The department code is required")
    @Size(max = CODE_MAX_LENGTH)
    String code,

    @Schema(description = "Optional description for the department")
    @Size(max = DESCRIPTION_MAX_LENGTH)
    String description,

    @Schema(description = "ID of the organization to which the department belongs (optional, must be set only if this is not a child comparment)")
    Long organizationId,

    @Schema(description = "ID of the parent department (optional, must be set only if this is a child department)")
    Long parentDepartmentId
) {
    @AssertTrue(message = "Either an organization or a parent department must be specified")
    public boolean orgOrCompValid() {
        return (organizationId != null && organizationId > 0) != (parentDepartmentId != null && parentDepartmentId > 0);
  }
}


