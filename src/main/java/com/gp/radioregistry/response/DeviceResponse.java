package com.gp.radioregistry.response;

import com.gp.radioregistry.domain.Device;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record DeviceResponse(
    @Schema(description = "Unique device ID")
    Long id,

    @Schema(description = "Device name")
    String name,

    @Schema(description = "Device type")
    Long deviceTypeId,

    @Schema(description = "Serial number")
    String serialNumber,

    @Schema(description = "Device description")
    String description,

    @Schema(description = "Installation date")
    LocalDate installationDate,

    @Schema(description = "Organization to which the device belongs")
    Long organizationId,

    @Schema(description = "Compartment to which the device belongs")
    Long compartmentId,

    @Schema(description = "Record creation date and time")
    OffsetDateTime createdAt,

    @Schema(description = "Record update date and time")
    OffsetDateTime updatedAt
) {
    public static DeviceResponse fromEntity(Device device) {
        if (device == null) {
            return null;
        }

        return new DeviceResponse(
                device.getId(),
                device.getName(),
                device.getDeviceType() != null ? device.getDeviceType().getId() : null,
                device.getSerialNumber(),
                device.getDescription(),
                device.getInstallationDate(),
                device.getOrganization() != null ? device.getOrganization().getId() : null,
                device.getCompartment() != null ? device.getCompartment().getId() : null,
                device.getCreatedAt(),
                device.getUpdatedAt()
        );
    }
}


