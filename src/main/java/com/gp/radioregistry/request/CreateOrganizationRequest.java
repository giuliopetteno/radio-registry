package com.gp.radioregistry.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrganizationRequest(
    @Schema(description = "Organization name, mandatory and cannot be empty")
    @NotBlank(message = "The organization name is mandatory")
    @Size(max = 50)
    String name,

    @Schema(description = "Organization code")
    @NotBlank(message = "The organization code is mandatory")
    @Size(max = 20)
    String code,

    @Schema(description = "Organization description")
    @Size(max = 200)
    String description

) {}


