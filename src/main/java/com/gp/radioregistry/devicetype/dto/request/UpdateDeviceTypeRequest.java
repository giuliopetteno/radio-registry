package com.gp.radioregistry.devicetype.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import static com.gp.radioregistry.constant.ValidationConstants.DESCRIPTION_MAX_LENGTH;
import static com.gp.radioregistry.constant.ValidationConstants.NAME_MAX_LENGTH;

public record UpdateDeviceTypeRequest(
	@Schema(description = "Device type name - if provided, updates the current value")
	@Size(max = NAME_MAX_LENGTH)
	String name,

	@Schema(description = "Description of the device type - if null, deletes the current value")
	@Size(max = DESCRIPTION_MAX_LENGTH)
	String description
) {}
