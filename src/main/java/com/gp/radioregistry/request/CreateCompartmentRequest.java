package com.gp.radioregistry.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.gp.radioregistry.constant.AppConstants.Validation.*;

public record CreateCompartmentRequest(
    @Schema(description = "Compartment name, mandatory and cannot be empty")
    @NotBlank(message = "The compartment name is required")
    @Size(max = NAME_MAX_LENGTH)
    String name,

    @Schema(description = "Compartment code, mandatory and cannot be empty")
    @NotBlank(message = "The compartment code is required")
    @Size(max = CODE_MAX_LENGTH)
    String code,

    @Schema(description = "Optional description for the compartment")
    @Size(max = DESCRIPTION_MAX_LENGTH)
    String description,

    @Schema(description = "ID of the organization to which the compartment belongs (optional, must be set only if this is not a child comparment)")
    Long organizationId,

    @Schema(description = "ID of the parent compartment (optional, must be set only if this is a child compartment)")
    Long parentCompartmentId

) {
    @AssertTrue(message = "Either an organization or a parent compartment must be specified")
    public boolean orgOrCompValid() {
        return (organizationId != null && organizationId > 0) != (parentCompartmentId != null && parentCompartmentId > 0);
  }
}


