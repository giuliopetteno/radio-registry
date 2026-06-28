package com.gp.radioregistry.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

import static com.gp.radioregistry.constant.AppConstants.DateTime.DEFAULT_DATE_FORMAT;
import static com.gp.radioregistry.constant.AppConstants.Validation.*;

public record CreateDeviceRequest(
    @Schema(description = "Device name")
    @NotBlank(message = "The name is mandatory")
    @Size(max = NAME_MAX_LENGTH)
    String name,

    @Schema(description = "ID of the device type")
    @NotNull(message = "The device type is mandatory")
    Long deviceTypeId,

    @Schema(description = "Device serial number")
    @NotBlank(message = "The serial number is mandatory")
    @Size(max = SERIAL_NUMBER_MAX_LENGTH)
    String serialNumber,

    @Schema(description = "Device description")
    @Size(max = DESCRIPTION_MAX_LENGTH)
    String description,

    @Schema(description = "Installation date and time")
    @NotNull(message = "The installation date is mandatory")
    @JsonFormat(pattern = DEFAULT_DATE_FORMAT)
    LocalDate installationDate,

    @Schema(description = "ID of the organization to which the device belongs")
    Long organizationId,

    @Schema(description = "ID of the compartment to which the device belongs")
    Long compartmentId

) {
    @AssertTrue(message = "Either an organization or a compartment must be specified")
    public boolean orgOrCompValid() {
        return (organizationId == null) != (compartmentId == null);
    }
}


