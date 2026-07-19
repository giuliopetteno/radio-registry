package com.gp.radioregistry.base;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

public abstract class AbstractIntegrationTest {

	@ServiceConnection
	static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18-alpine");

	@ServiceConnection
	static final KafkaContainer KAFKA = new KafkaContainer("apache/kafka:4.3.1");

	static {
		POSTGRES.start();
		KAFKA.start();
	}
}
