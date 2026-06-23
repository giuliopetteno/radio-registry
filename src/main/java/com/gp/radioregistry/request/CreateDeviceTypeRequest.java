package com.gp.radioregistry.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDeviceTypeRequest(
    @Schema(description = "The unique identifier name of the device type. Cannot be empty.")
    @NotBlank(message = "The name is mandatory and cannot contain only whitespace")
    @Size(max = 50)
    String name,

    @Schema(description = "Description of the device type.")
    @Size(max = 200)
    String description
){}

