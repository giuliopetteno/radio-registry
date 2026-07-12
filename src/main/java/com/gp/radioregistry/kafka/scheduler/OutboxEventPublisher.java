package com.gp.radioregistry.kafka.scheduler;

import com.gp.radioregistry.kafka.outboxevent.domain.OutboxEvent;
import com.gp.radioregistry.kafka.outboxevent.enums.OutboxEventStatus;
import com.gp.radioregistry.kafka.outboxevent.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

	private final OutboxEventRepository outboxEventRepository;
	private final KafkaTemplate<String, String> kafkaTemplate;

	@Scheduled(fixedDelayString = "${outbox.publisher.fixed-delay}")
	@Transactional
	public void publishPendingEvents() {
		List<OutboxEvent> pendingEvents = outboxEventRepository.findTop50ByOutboxEventStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING);

		for (OutboxEvent event : pendingEvents) {
			try {
				kafkaTemplate.send(event.getEntityType().toLowerCase(Locale.ROOT) + "-events", event.getEntityId(), event.getPayload()).get();
				event.setOutboxEventStatus(OutboxEventStatus.PROCESSED);
				event.setProcessedAt(OffsetDateTime.now());
			} catch (Exception e) {
				log.error("Failed to publish outbox event {}", event.getId(), e);
				event.setOutboxEventStatus(OutboxEventStatus.FAILED);
			}
		}
		outboxEventRepository.saveAll(pendingEvents);
	}
}
