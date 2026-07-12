package com.gp.radioregistry.kafka.outboxevent.repository;

import com.gp.radioregistry.kafka.outboxevent.enums.OutboxEventStatus;
import com.gp.radioregistry.kafka.outboxevent.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.OffsetDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
	List<OutboxEvent> findTop50ByOutboxEventStatusOrderByCreatedAtAsc(OutboxEventStatus outboxEventStatus);

	List<OutboxEvent> findTop50ByOutboxEventStatusInOrderByCreatedAtAsc(List<OutboxEventStatus> statuses);

	@Modifying
	int deleteByOutboxEventStatusAndProcessedAtBefore(OutboxEventStatus outboxEventStatus, OffsetDateTime dateTime);
}
