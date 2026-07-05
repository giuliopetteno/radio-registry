package com.gp.radioregistry.audit.aspect;

import com.gp.radioregistry.audit.annotation.Auditable;
import com.gp.radioregistry.audit.auditlog.domain.AuditLog;
import com.gp.radioregistry.audit.auditlog.service.AuditLogService;
import com.gp.radioregistry.audit.enums.AuditAction;
import com.gp.radioregistry.security.auth.dto.request.LoginRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {
	private final AuditLogService auditLogService;

	@Around("@annotation(auditable)")
	public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
		AuditLog auditLog = new AuditLog();
		Object[] args = joinPoint.getArgs();

		if (auditable.action() == AuditAction.LOGIN && args.length > 0 && args[0] instanceof LoginRequest loginRequest) {
			auditLog.setUsername(loginRequest.username());
		} else {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
				setAuthenticationDetails(auditLog, authentication);
			}
		}

		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		auditLog.setIpAddress(servletRequestAttributes != null ? servletRequestAttributes.getRequest().getRemoteAddr() : null);

		auditLog.setAction(auditable.action().toString());
		auditLog.setEntityType(auditable.entityType().toString());

		if(args.length > 0 && args[0] instanceof Long id)
			auditLog.setEntityId(id.toString());

		auditLog.setDescription(auditable.description());

		Object result;
		try {
			result = joinPoint.proceed();
			if (auditable.action() == AuditAction.LOGIN && result instanceof Authentication loginAuthentication) {
				setAuthenticationDetails(auditLog, loginAuthentication);
			}
			if (auditLog.getEntityId() == null) {
				try {
					Method getId = result.getClass().getMethod("getId");
					Object id = getId.invoke(result);
					auditLog.setEntityId(id != null ? id.toString() : null);
				} catch (Exception e) {
					log.warn("Could not resolve entityId via getId(): {}", e.getMessage());
				}
			}
			auditLog.setSuccess(true);
		} catch (Throwable ex) {
			auditLog.setSuccess(false);
			auditLog.setErrorDetail(ex.getMessage());
			throw ex;
		} finally {
			auditLogService.saveAuditLog(auditLog);
		}

		return result;
	}

	private void setAuthenticationDetails(AuditLog auditLog, Authentication authentication) {
		auditLog.setUsername(authentication.getName());
		auditLog.setUserRoles(
			authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.filter(Objects::nonNull)
				.filter(authority -> authority.startsWith("ROLE_"))
				.map(authority -> "\"" + authority + "\"")
				.collect(Collectors.joining(",", "[", "]"))
		);
	}
}
