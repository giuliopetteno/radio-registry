package com.gp.radioregistry.kafka.scheduler;

import com.gp.radioregistry.kafka.outboxevent.enums.OutboxEventStatus;
import com.gp.radioregistry.kafka.outboxevent.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventCleanup {

	@Value("${outbox.retention.days}")
	private int retentionDays;

	private final OutboxEventRepository outboxEventRepository;

	@Scheduled(cron = "${outbox.cleanup.cron}")
	@Transactional
	public void cleanupProcessedEvents() {
		try {
			int numEventsDeleted = outboxEventRepository.deleteByOutboxEventStatusAndProcessedAtBefore(
				OutboxEventStatus.PROCESSED, OffsetDateTime.now().minusDays(retentionDays));
			log.info("Cleaned up {} processed outbox events older than {} days", numEventsDeleted, retentionDays);
		} catch (Exception e) {
			log.error("Failed to clean up processed outbox events", e);
		}
	}
}
