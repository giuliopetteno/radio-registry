package com.gp.radioregistry.audit.auditlog.dto;

public record AuditLogEvent(
	Long userId,

	String username,

	String userRoles,

	String ipAddress,

	String action,

	String entityType,

	String entityId,

	String description,

	String oldValue,

	String newValue,

	boolean success,

	String errorDetail
) {}
