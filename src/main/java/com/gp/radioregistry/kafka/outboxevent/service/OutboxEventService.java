package com.gp.radioregistry.kafka.outboxevent.service;

import com.gp.radioregistry.kafka.outboxevent.domain.OutboxEvent;
import com.gp.radioregistry.kafka.outboxevent.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

	private final OutboxEventRepository outboxEventRepository;
	private final JsonMapper jsonMapper;

	public void save(String entityType, String entityId, String eventType, Object payload) {
		var serializedPayload = jsonMapper.writeValueAsString(payload);
		var outboxEvent = OutboxEvent.builder()
			.entityType(entityType)
			.entityId(entityId)
			.eventType(eventType)
			.payload(serializedPayload)
			.build();
		outboxEventRepository.save(outboxEvent);
	}
}
