package com.gp.radioregistry.audit.auditlog.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "audit_logs")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "username", length = 255)
	private String username;

	@Column(name = "user_roles", columnDefinition = "jsonb")
	private String userRoles;

	@Column(name = "ip_address", length = 45)
	private String ipAddress;

	@Column(name = "action", length = 50, nullable = false)
	private String action;

	@Column(name = "entity_type", length = 100)
	private String entityType;

	@Column(name = "entity_id", length = 50)
	private String entityId;

	@Column(name = "description", columnDefinition = "text")
	private String description;

	@Column(name = "old_value", columnDefinition = "jsonb")
	private String oldValue;

	@Column(name = "new_value", columnDefinition = "jsonb")
	private String newValue;

	@Column(name = "success", nullable = false)
	private boolean success;

	@Column(name = "error_detail", columnDefinition = "text")
	private String errorDetail;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;
}
