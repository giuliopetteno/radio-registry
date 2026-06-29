package com.gp.radioregistry.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.gp.radioregistry.constant.AppConstants.Validation.DESCRIPTION_MAX_LENGTH;
import static com.gp.radioregistry.constant.AppConstants.Validation.NAME_MAX_LENGTH;

public record CreateDeviceTypeRequest(
    @Schema(description = "The unique identifier name of the device type. Cannot be empty.")
    @NotBlank(message = "The device type name is required")
    @Size(max = NAME_MAX_LENGTH)
    String name,

    @Schema(description = "Optional description for the device type.")
    @Size(max = DESCRIPTION_MAX_LENGTH)
    String description
){}

