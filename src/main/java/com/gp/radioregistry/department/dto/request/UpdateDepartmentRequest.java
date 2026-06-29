package com.gp.radioregistry.department.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

import static com.gp.radioregistry.constant.ValidationConstants.*;

public record UpdateDepartmentRequest(
	@Schema(description = "Department name - if provided, updates the current value")
	@Size(max = NAME_MAX_LENGTH)
	String name,

	@Schema(description = "Department code - if provided, updates the current value")
	@Size(max = CODE_MAX_LENGTH)
	String code,

	@Schema(description = "Description for the department - if null, deletes the current value")
	@Size(max = DESCRIPTION_MAX_LENGTH)
	String description,

	@Schema(description = "ID of the organization to which the department belongs - if null, deletes the current value")
	Long organizationId,

	@Schema(description = "ID of the parent department - if null, deletes the current value")
	Long parentDepartmentId
) {
	@AssertTrue(message = "Either an organization or a parent department must be specified")
	public boolean orgOrCompValid() {
		return (organizationId != null && organizationId > 0) != (parentDepartmentId != null && parentDepartmentId > 0);
	}
}
