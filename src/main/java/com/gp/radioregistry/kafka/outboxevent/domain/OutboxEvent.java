package com.gp.radioregistry.kafka.outboxevent.domain;

import com.gp.radioregistry.kafka.enums.OutboxStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Table(name = "outbox_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "entity_type", nullable = false)
	private String entityType;

	@Column(name = "entity_id", nullable = false)
	private String entityId;

	@Column(name = "event_type", nullable = false)
	private String eventType;

	@Column(name = "payload", columnDefinition = "jsonb", nullable = false)
	@JdbcTypeCode(SqlTypes.JSON)
	private String payload;

	@Enumerated(EnumType.STRING)
	@Column(name = "outbox_status", nullable = false)
	private OutboxStatus outboxStatus = OutboxStatus.PENDING;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "processed_at")
	private OffsetDateTime processedAt;
}
