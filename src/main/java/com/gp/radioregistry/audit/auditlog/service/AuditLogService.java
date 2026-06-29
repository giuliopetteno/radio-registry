package com.gp.radioregistry.audit.auditlog.service;

import com.gp.radioregistry.audit.auditlog.domain.AuditLog;
import com.gp.radioregistry.audit.auditlog.dto.AuditLogEvent;
import com.gp.radioregistry.audit.auditlog.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {
	private final AuditLogRepository auditLogRepository;

	@Async("auditTaskExecutor")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveAuditLog(AuditLogEvent event) {
		try {
			var auditLog = AuditLog.builder()
				.userId(event.userId())
				.username(event.username())
				.userRoles(event.userRoles())
				.ipAddress(event.ipAddress())
				.action(event.action())
				.entityType(event.entityType())
				.entityId(event.entityId())
				.description(event.description())
				.oldValue(event.oldValue())
				.newValue(event.newValue())
				.success(event.success())
				.errorDetail(event.errorDetail())
				.build();

			auditLogRepository.save(auditLog);
		} catch (Exception e) {
			log.error("Failed to persist audit log transaction: {}", e.getMessage());
		}
	}
}
