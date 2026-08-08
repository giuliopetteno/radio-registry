package com.gp.radioregistry.kafka.event;

import com.gp.radioregistry.enums.EventType;
import com.gp.radioregistry.organization.domain.Organization;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record OrganizationEvent(
	EventType eventType,
	UUID eventId,
	Long organizationId,
	String name,
	String code,
	String description,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt,
	Instant producedAt
) {
	public static OrganizationEvent of(EventType eventType, UUID eventId, Organization organization) {
		return new OrganizationEvent(
			eventType,
			eventId,
			organization.getId(),
			organization.getName(),
			organization.getCode(),
			organization.getDescription(),
			organization.getCreatedAt(),
			organization.getUpdatedAt(),
			Instant.now()
		);
	}
}
