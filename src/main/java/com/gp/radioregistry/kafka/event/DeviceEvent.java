package com.gp.radioregistry.kafka.event;

import com.gp.radioregistry.device.domain.Device;
import com.gp.radioregistry.enums.EventType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record DeviceEvent(
	EventType eventType,
	UUID eventId,
	Long deviceId,
	String name,
	Long deviceTypeId,
	String serialNumber,
	String description,
	LocalDate installationDate,
	String deviceStatus,
	LocalDate decommissionDate,
	Long organizationId,
	Long departmentId,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt,
	Instant producedAt
) {
	public static DeviceEvent of(EventType eventType, UUID eventId, Device device) {
		return new DeviceEvent(
			eventType,
			eventId,
			device.getId(),
			device.getName(),
			device.getDeviceType().getId(),
			device.getSerialNumber(),
			device.getDescription(),
			device.getInstallationDate(),
			device.getDeviceStatus().name(),
			device.getDecommissionDate(),
			device.getOrganization() != null ? device.getOrganization().getId() : null,
			device.getDepartment() != null ? device.getDepartment().getId() : null,
			device.getCreatedAt(),
			device.getUpdatedAt(),
			Instant.now()
		);
	}
}
