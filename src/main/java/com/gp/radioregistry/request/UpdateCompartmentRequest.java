package com.gp.radioregistry.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

import static com.gp.radioregistry.constant.AppConstants.Validation.*;

public record UpdateCompartmentRequest(
	@Schema(description = "Compartment name - if provided, updates the current value")
	@Size(max = NAME_MAX_LENGTH)
	String name,

	@Schema(description = "Compartment code - if provided, updates the current value")
	@Size(max = CODE_MAX_LENGTH)
	String code,

	@Schema(description = "Description for the compartment - if null, deletes the current value")
	@Size(max = DESCRIPTION_MAX_LENGTH)
	String description,

	@Schema(description = "ID of the organization to which the compartment belongs - if null, deletes the current value")
	Long organizationId,

	@Schema(description = "ID of the parent compartment - if null, deletes the current value")
	Long parentCompartmentId

) {
	@AssertTrue(message = "Either an organization or a parent compartment must be specified")
	public boolean orgOrCompValid() {
		return (organizationId != null && organizationId > 0) != (parentCompartmentId != null && parentCompartmentId > 0);
	}
}
