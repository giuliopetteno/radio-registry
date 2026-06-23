package com.gp.radioregistry.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCompartmentRequest(
    @Schema(description = "Compartment name, mandatory and cannot be empty")
    @NotBlank(message = "The compartment name is mandatory")
    @Size(max = 50)
    String name,

    @Schema(description = "Compartment code, mandatory and cannot be empty")
    @NotBlank(message = "The compartment code is mandatory")
    @Size(max = 20)
    String code,

    @Schema(description = "Optional description for the compartment")
    @Size(max = 200)
    String description,

    @Schema(description = "ID of the organization to which the compartment belongs")
    Long organizationId,

    @Schema(description = "ID of the parent compartment (optional, must be set only if this is a child compartment)")
    Long parentCompartmentId

) {
    @AssertTrue(message = "Either an organization or a parent compartment must be specified")
    public boolean orgOrCompValid() {
        return (organizationId == null) != (parentCompartmentId == null);
    }
}


