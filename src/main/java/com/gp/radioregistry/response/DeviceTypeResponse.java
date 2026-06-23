package com.gp.radioregistry.response;

import com.gp.radioregistry.domain.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

public record DeviceTypeResponse(
    @Schema(description = "Unique device type ID")
    Long id,

    @Schema(description = "Device type name")
    String name,

    @Schema(description = "Device type description")
    String description,

    @Schema(description = "Record creation date and time")
    OffsetDateTime createdAt,

    @Schema(description = "Record update date and time")
    OffsetDateTime updatedAt
) {
    public static DeviceTypeResponse fromEntity(DeviceType deviceType) {
        if (deviceType == null) {
            return null;
        }

        return new DeviceTypeResponse(
                deviceType.getId(),
                deviceType.getName(),
                deviceType.getDescription(),
                deviceType.getCreatedAt(),
                deviceType.getUpdatedAt()
        );
    }
}


