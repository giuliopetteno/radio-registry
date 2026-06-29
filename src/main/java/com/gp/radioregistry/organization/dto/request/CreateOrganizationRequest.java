package com.gp.radioregistry.organization.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.gp.radioregistry.constant.ValidationConstants.*;

public record CreateOrganizationRequest(
    @Schema(description = "Organization name, mandatory and cannot be empty")
    @NotBlank(message = "The organization name is required")
    @Size(max = NAME_MAX_LENGTH)
    String name,

    @Schema(description = "Organization code")
    @NotBlank(message = "The organization code is required")
    @Size(max = CODE_MAX_LENGTH)
    String code,

    @Schema(description = "Optional description for the organization")
    @Size(max = DESCRIPTION_MAX_LENGTH)
    String description
) {}


