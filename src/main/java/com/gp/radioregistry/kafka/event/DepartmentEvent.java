package com.gp.radioregistry.kafka.event;

import com.gp.radioregistry.department.domain.Department;
import com.gp.radioregistry.enums.EventType;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record DepartmentEvent(
	EventType eventType,
	UUID eventId,
	Long departmentId,
	String name,
	String code,
	String description,
	Long organizationId,
	Long parentDepartmentId,
	OffsetDateTime createdAt,
	OffsetDateTime updatedAt,
	Instant producedAt
) {
	public static DepartmentEvent of(EventType eventType, UUID eventId, Department department) {
		return new DepartmentEvent(
			eventType,
			eventId,
			department.getId(),
			department.getName(),
			department.getCode(),
			department.getDescription(),
			department.getOrganization() != null ? department.getOrganization().getId() : null,
			department.getParentDepartment() != null ? department.getParentDepartment().getId() : null,
			department.getCreatedAt(),
			department.getUpdatedAt(),
			Instant.now()
		);
	}
}
