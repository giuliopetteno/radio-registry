package com.gp.radioregistry.base;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.kafka.KafkaContainer;

public abstract class AbstractKafkaContainerTest {

	@ServiceConnection
	static final KafkaContainer KAFKA = new KafkaContainer("apache/kafka:4.3.1");

	static {
		KAFKA.start();
	}
}
