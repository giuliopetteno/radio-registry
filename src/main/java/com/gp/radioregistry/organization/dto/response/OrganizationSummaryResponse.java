package com.gp.radioregistry.organization.dto.response;

import com.gp.radioregistry.organization.domain.Organization;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

public record OrganizationSummaryResponse(
    @Schema(description = "Unique organization ID")
    Long id,

    @Schema(description = "Organization name")
    String name,

    @Schema(description = "Organization code")
    String code,

    @Schema(description = "Organization description")
    String description,

    @Schema(description = "Organization creation date and time")
    OffsetDateTime createdAt,

    @Schema(description = "Organization update date and time")
    OffsetDateTime updatedAt
) {
    public static OrganizationSummaryResponse fromEntity(Organization organization) {
        if (organization == null) {
            return null;
        }

        return new OrganizationSummaryResponse(
                organization.getId(),
                organization.getName(),
                organization.getCode(),
                organization.getDescription(),
                organization.getCreatedAt(),
                organization.getUpdatedAt()
        );
    }
}

