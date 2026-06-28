package com.gp.radioregistry.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.gp.radioregistry.constant.AppConstants.Validation.*;

public record CreateOrganizationRequest(
    @Schema(description = "Organization name, mandatory and cannot be empty")
    @NotBlank(message = "The organization name is mandatory")
    @Size(max = NAME_MAX_LENGTH)
    String name,

    @Schema(description = "Organization code")
    @NotBlank(message = "The organization code is mandatory")
    @Size(max = CODE_MAX_LENGTH)
    String code,

    @Schema(description = "Organization description")
    @Size(max = DESCRIPTION_MAX_LENGTH)
    String description

) {}


