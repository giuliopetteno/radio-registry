package com.gp.radioregistry.response;

import com.gp.radioregistry.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

public record RoleResponse(
    @Schema(description = "Unique device type ID")
    Long id,

    @Schema(description = "Device type name")
    String name,

    @Schema(description = "Record creation date and time")
    OffsetDateTime createdAt,

    @Schema(description = "Record update date and time")
    OffsetDateTime updatedAt
) {
    public static RoleResponse fromEntity(Role role) {
        if (role == null) {
            return null;
        }

        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }
}


