package com.gp.radioregistry.audit.aspect;

import com.gp.radioregistry.audit.annotation.Auditable;
import com.gp.radioregistry.audit.auditlog.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

	private final AuditLogService auditLogService;

	@Around("@annotation(auditable)")
	public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {

	return null;
	}

}
