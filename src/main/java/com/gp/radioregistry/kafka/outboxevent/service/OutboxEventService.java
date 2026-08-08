package com.gp.radioregistry.kafka.outboxevent.service;

import com.gp.radioregistry.enums.EntityType;
import com.gp.radioregistry.enums.EventType;
import com.gp.radioregistry.kafka.outboxevent.domain.OutboxEvent;
import com.gp.radioregistry.kafka.outboxevent.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

	private final OutboxEventRepository outboxEventRepository;
	private final JsonMapper jsonMapper;

	public <T> void saveOutboxEvent(EntityType entityType, Long entityId, EventType eventType, Function<UUID, T> payloadFactory) {
		UUID eventId = UUID.randomUUID();
		T payload = payloadFactory.apply(eventId);

		var outboxEvent = OutboxEvent.builder()
			.entityType(entityType.name())
			.entityId(String.valueOf(entityId))
			.eventType(eventType.name())
			.eventId(eventId)
			.payload(jsonMapper.writeValueAsString(payload))
			.build();
		outboxEventRepository.save(outboxEvent);
	}
}
