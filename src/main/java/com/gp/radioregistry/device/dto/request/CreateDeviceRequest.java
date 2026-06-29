package com.gp.radioregistry.device.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

import static com.gp.radioregistry.constant.DateTimeConstants.DEFAULT_DATE_FORMAT;
import static com.gp.radioregistry.constant.ValidationConstants.*;

public record CreateDeviceRequest(
    @Schema(description = "Device name")
    @NotBlank(message = "The device name is required")
    @Size(max = NAME_MAX_LENGTH)
    String name,

    @Schema(description = "ID of the device type")
    @NotNull(message = "The device type is required")
    Long deviceTypeId,

    @Schema(description = "Device serial number")
    @NotBlank(message = "The device serial number is required")
    @Size(max = SERIAL_NUMBER_MAX_LENGTH)
    String serialNumber,

    @Schema(description = "Optional description for the device")
    @Size(max = DESCRIPTION_MAX_LENGTH)
    String description,

    @Schema(description = "Installation date")
    @NotNull(message = "The device installation date is required")
    @JsonFormat(pattern = DEFAULT_DATE_FORMAT)
    LocalDate installationDate,

    @Schema(description = "ID of the organization to which the device belongs")
    Long organizationId,

    @Schema(description = "ID of the department to which the device belongs")
    Long departmentId
) {
    @AssertTrue(message = "Either an organization or a department must be specified")
    public boolean orgOrCompValid() {
        return (organizationId != null && organizationId > 0) != (departmentId != null && departmentId > 0);
  }
}


