package com.gp.radioregistry.audit.auditlog.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "username", length = 100)
	private String username;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "user_roles", columnDefinition = "jsonb")
	private String userRoles;

	@Column(name = "ip_address", length = 45)
	private String ipAddress;

	@Column(name = "event_type", length = 50, nullable = false)
	private String eventType;

	@Column(name = "entity_type", length = 100)
	private String entityType;

	@Column(name = "entity_id", length = 50)
	private String entityId;

	@Column(name = "description", length = 200)
	private String description;

	@Column(name = "success", nullable = false)
	private boolean success;

	@Column(name = "error_detail", columnDefinition = "text")
	private String errorDetail;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;
}
