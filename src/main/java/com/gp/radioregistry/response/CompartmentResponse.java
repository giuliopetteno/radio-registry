package com.gp.radioregistry.response;

import com.gp.radioregistry.domain.Compartment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

public record CompartmentResponse(
    @Schema(description = "Unique compartment ID")
    Long id,

    @Schema(description = "Compartment name")
    String name,

    @Schema(description = "Unique identification code")
    String code,

    @Schema(description = "Compartment description")
    String description,

    @Schema(description = "Organization to which the compartment belongs")
    Long organizationId,

    @Schema(description = "Parent compartment (if exists) to which this child compartment belongs")
    Long parentCompartmentId,

    @Schema(description = "Child compartments of this compartment")
    List<CompartmentResponse> childCompartments,

    @Schema(description = "Devices related to the compartment")
    List<DeviceResponse> devices,

    @Schema(description = "Record creation date and time")
    OffsetDateTime createdAt,

    @Schema(description = "Record update date and time")
    OffsetDateTime updatedAt
) {
    public static CompartmentResponse fromEntity(Compartment compartment) {
        if (compartment == null) {
            return null;
        }

        return new CompartmentResponse(
                compartment.getId(),
                compartment.getName(),
                compartment.getCode(),
                compartment.getDescription(),
                compartment.getOrganization() != null ? compartment.getOrganization().getId() : null,
                compartment.getParentCompartment() != null ? compartment.getParentCompartment().getId() : null,
                compartment.getChildCompartments().stream().map(CompartmentResponse::fromEntity).toList(),
                compartment.getDevices().stream().map(DeviceResponse::fromEntity).toList(),
                compartment.getCreatedAt(),
                compartment.getUpdatedAt()
        );
    }
}

