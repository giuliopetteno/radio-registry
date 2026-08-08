package com.gp.radioregistry.kafka.event;

import com.gp.radioregistry.devicetype.domain.DeviceType;
import com.gp.radioregistry.enums.EventType;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record DeviceTypeEvent(
	EventType eventType,
	UUID eventId,
	Long deviceTypeId,
	String name,
	String description,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt,
	Instant producedAt
) {
	public static DeviceTypeEvent of(EventType eventType, UUID eventId, DeviceType deviceType) {
		return new DeviceTypeEvent(
			eventType,
			eventId,
			deviceType.getId(),
			deviceType.getName(),
			deviceType.getDescription(),
			deviceType.getCreatedAt(),
			deviceType.getUpdatedAt(),
			Instant.now()
		);
	}
}
