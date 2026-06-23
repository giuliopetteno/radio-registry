package com.gp.radioregistry.response;

import com.gp.radioregistry.domain.Organization;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

public record OrganizationResponse(
    @Schema(description = "Unique organization ID")
    Long id,

    @Schema(description = "Organization name")
    String name,

    @Schema(description = "Organization code")
    String code,

    @Schema(description = "Organization description")
    String description,

    @Schema(description = "Compartments belonging to the organization")
    List<CompartmentResponse> compartments,

    @Schema(description = "Devices belonging to the organization")
    List<DeviceResponse> devices,

    @Schema(description = "Organization creation date and time")
    OffsetDateTime createdAt,

    @Schema(description = "Organization update date and time")
    OffsetDateTime updatedAt
) {
    public static OrganizationResponse fromEntity(Organization organization) {
        if (organization == null) {
            return null;
        }

        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getCode(),
                organization.getDescription(),
                organization.getCompartments().stream()
                        .map(CompartmentResponse::fromEntity)
                        .toList(),
                organization.getDevices().stream()
                        .map(DeviceResponse::fromEntity)
                        .toList(),
                organization.getCreatedAt(),
                organization.getUpdatedAt()
        );
    }
}

