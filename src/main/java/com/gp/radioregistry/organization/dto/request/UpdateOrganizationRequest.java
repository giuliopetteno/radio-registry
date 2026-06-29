package com.gp.radioregistry.organization.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import static com.gp.radioregistry.constant.ValidationConstants.*;

public record UpdateOrganizationRequest(
	@Schema(description = "Organization name - if provided, updates the current value")
	@Size(max = NAME_MAX_LENGTH)
	String name,

	@Schema(description = "Organization code - if provided, updates the current value")
	@Size(max = CODE_MAX_LENGTH)
	String code,

	@Schema(description = "Organization description - if null, deletes the current value")
	@Size(max = DESCRIPTION_MAX_LENGTH)
	String description
) {}
