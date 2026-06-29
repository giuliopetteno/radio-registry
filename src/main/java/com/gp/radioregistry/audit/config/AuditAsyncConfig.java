package com.gp.radioregistry.audit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AuditAsyncConfig {

	@Bean(name = "auditTaskExecutor")
	public Executor auditTaskExecutor() {
		var executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(6);
		executor.setQueueCapacity(300);
		executor.setThreadNamePrefix("audit-");
		executor.initialize();
		return executor;
	}
}
