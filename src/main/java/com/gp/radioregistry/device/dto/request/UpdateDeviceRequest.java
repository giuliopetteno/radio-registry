package com.gp.radioregistry.device.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gp.radioregistry.device.enums.DeviceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

import static com.gp.radioregistry.constant.DateTimeConstants.DEFAULT_DATE_FORMAT;
import static com.gp.radioregistry.constant.ValidationConstants.*;

public record UpdateDeviceRequest(
	@Schema(description = "Device name - if provided, updates the current value")
	@Size(max = NAME_MAX_LENGTH)
	String name,

	@Schema(description = "ID of the device type - if provided, updates the current value")
	Long deviceTypeId,

	@Schema(description = "Device serial number - if provided, updates the current value")
	@Size(max = SERIAL_NUMBER_MAX_LENGTH)
	String serialNumber,

	@Schema(description = "Device description - if null, deletes the current value")
	@Size(max = DESCRIPTION_MAX_LENGTH)
	String description,

	@Schema(description = "Installation date - if provided, updates the current value")
	@JsonFormat(pattern = DEFAULT_DATE_FORMAT)
	LocalDate installationDate,

	@Schema(description = "Status of the device - if provided, updates the current value")
	@NotNull(message = "Device status must not be null")
	DeviceStatus deviceStatus,

	@Schema(description = "Decommission date - if null, deletes the current value")
	@JsonFormat(pattern = DEFAULT_DATE_FORMAT)
	LocalDate decommissionDate,

	@Schema(description = "ID of the organization to which the device belongs - if null, deletes the current value")
	Long organizationId,

	@Schema(description = "ID of the department to which the device belongs - if null, deletes the current value")
	Long departmentId
) {
	@AssertTrue(message = "Either an organization or a department must be specified")
	public boolean isOrgOrCompValid() {
		return (organizationId != null && organizationId > 0) != (departmentId != null && departmentId > 0);
	}

	@AssertTrue(message = "A decommission date must be specified if the status is DECOMMISSIONED or PENDING_DECOMMISSIONING")
	public boolean idDecommissionDateValid() {
		boolean isDecommissionState = deviceStatus == DeviceStatus.DECOMMISSIONED
			|| deviceStatus == DeviceStatus.PENDING_DECOMMISSIONING;
		return isDecommissionState == (decommissionDate != null);
	}
}
