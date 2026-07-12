package com.gp.radioregistry.kafka.scheduler;

import com.gp.radioregistry.kafka.outboxevent.domain.OutboxEvent;
import com.gp.radioregistry.kafka.outboxevent.enums.OutboxEventStatus;
import com.gp.radioregistry.kafka.outboxevent.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.DisconnectException;
import org.apache.kafka.common.errors.NetworkException;
import org.apache.kafka.common.errors.TimeoutException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

	private final OutboxEventRepository outboxEventRepository;
	private final KafkaTemplate<String, String> kafkaTemplate;

	private static final String TOPIC_SUFFIX = "-events";

	@Value("${outbox.publisher.max-retries}")
	private int maxRetries;

	@Scheduled(fixedDelayString = "${outbox.publisher.fixed-delay}")
	@Transactional
	public void publishOutboxEvents() {
		List<OutboxEvent> toSave = new ArrayList<>();

		List<OutboxEvent> outboxEvents = outboxEventRepository
			.findTop50ByOutboxEventStatusInOrderByCreatedAtAsc(List.of(OutboxEventStatus.PENDING, OutboxEventStatus.FAILED));

		for (OutboxEvent event : outboxEvents) {
			try {
				kafkaTemplate.send(
					event.getEntityType().toLowerCase(Locale.ROOT) + TOPIC_SUFFIX,
					event.getEntityId(),
					event.getPayload()
				).get(10, TimeUnit.SECONDS);

				event.setOutboxEventStatus(OutboxEventStatus.PROCESSED);
				event.setProcessedAt(OffsetDateTime.now());
				toSave.add(event);
			} catch (Exception e) {
				if (isBrokerUnavailable(e)) {
					log.warn("Kafka broker unavailable, skipping retry count for event {}", event.getId(), e);
					continue;
				}

				int attempts = event.getRetryCount() + 1;
				event.setRetryCount(attempts);

				if (attempts >= maxRetries) {
					event.setOutboxEventStatus(OutboxEventStatus.DEAD_LETTER);
					log.error("Outbox event {} moved to DEAD_LETTER after {} attempts", event.getId(), attempts, e);
				} else {
					event.setOutboxEventStatus(OutboxEventStatus.FAILED);
					log.warn("Failed to publish outbox event {} (attempt {}/{})", event.getId(), attempts, maxRetries, e);
				}
				toSave.add(event);
			}
		}
		outboxEventRepository.saveAll(toSave);
	}

	private boolean isBrokerUnavailable(Throwable e) {
		Throwable cause = e.getCause() != null ? e.getCause() : e;
		return cause instanceof TimeoutException
			|| cause instanceof DisconnectException
			|| cause instanceof NetworkException;
	}
}
