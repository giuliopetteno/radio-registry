package com.gp.radioregistry.audit.aspect;

import com.gp.radioregistry.audit.annotation.Auditable;
import com.gp.radioregistry.audit.auditlog.domain.AuditLog;
import com.gp.radioregistry.audit.auditlog.service.AuditLogService;
import com.gp.radioregistry.audit.enums.AuditAction;
import com.gp.radioregistry.audit.enums.AuditEntityType;
import com.gp.radioregistry.security.auth.dto.request.LoginRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.Annotation;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditAspect unit tests")
class AuditAspectTest {

	private static final String MOCK_IP = "203.0.113.5";
	private static final String USERNAME = "user";
	private static final String USERNAME_ANONYMOUS = "anonymousUser";
	private static final String PASSWORD = "password";
	private static final String PASSWORD_ANONYMOUS = "anonymousPassword";
	private static final Long ENTITY_ID = 99L;

	@Mock
	private AuditLogService auditLogService;

	@Mock
	private ProceedingJoinPoint joinPoint;

	@Captor
	private ArgumentCaptor<AuditLog> auditLogCaptor;

	private AuditAspect auditAspect;

	@BeforeEach
	void setUp() {
		auditAspect = new AuditAspect(auditLogService);
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
		RequestContextHolder.resetRequestAttributes();
	}

	private Auditable auditable(AuditAction action, AuditEntityType entityType, String description) {
		return new Auditable() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return Auditable.class;
			}

			@Override
			public AuditAction action() {
				return action;
			}

			@Override
			public AuditEntityType entityType() {
				return entityType;
			}

			@Override
			public String entityId() {
				return "";
			}

			@Override
			public String description() {
				return description;
			}
		};
	}

	private void setSecurityContext(Authentication authentication) {
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private void setRequestWithRemoteAddr(String remoteAddr) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRemoteAddr(remoteAddr);
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
	}

	@Getter
	@AllArgsConstructor
	private static class EntityResult {
		private final Long id;
	}

	@Nested
	@DisplayName("Successful invocation")
	class SuccessfulInvocation {

		@Test
		@DisplayName("proceeds, marks success and saves the audit log")
		void proceedsAndSavesAuditLog() throws Throwable {
			Auditable meta = auditable(AuditAction.READ, AuditEntityType.DEVICE, "read device");
			Object expected = new Object();
			when(joinPoint.getArgs()).thenReturn(new Object[0]);
			when(joinPoint.proceed()).thenReturn(expected);

			Object result = auditAspect.audit(joinPoint, meta);

			assertThat(result).isSameAs(expected);
			verify(joinPoint).proceed();
			verify(auditLogService).saveAuditLog(auditLogCaptor.capture());

			AuditLog log = auditLogCaptor.getValue();
			assertThat(log.isSuccess()).isTrue();
			assertThat(log.getErrorDetail()).isNull();
			assertThat(log.getAction()).isEqualTo(AuditAction.READ.toString());
			assertThat(log.getEntityType()).isEqualTo(AuditEntityType.DEVICE.toString());
			assertThat(log.getDescription()).isEqualTo("read device");
		}

		@Test
		@DisplayName("sets entityId from a Long first argument")
		void setsEntityIdFromLongArgument() throws Throwable {
			Auditable meta = auditable(AuditAction.DELETE, AuditEntityType.ROLE, "delete role");
			when(joinPoint.getArgs()).thenReturn(new Object[]{ENTITY_ID});
			when(joinPoint.proceed()).thenReturn(null);

			auditAspect.audit(joinPoint, meta);

			verify(auditLogService).saveAuditLog(auditLogCaptor.capture());
			assertThat(auditLogCaptor.getValue().getEntityId()).isEqualTo(ENTITY_ID.toString());
		}

		@Test
		@DisplayName("resolves entityId via getId() on the result when no Long argument is present")
		void resolvesEntityIdFromResultGetId() throws Throwable {
			Auditable meta = auditable(AuditAction.CREATE, AuditEntityType.DEVICE, "create device");
			when(joinPoint.getArgs()).thenReturn(new Object[0]);
			when(joinPoint.proceed()).thenReturn(new EntityResult(ENTITY_ID));

			auditAspect.audit(joinPoint, meta);

			verify(auditLogService).saveAuditLog(auditLogCaptor.capture());
			assertThat(auditLogCaptor.getValue().getEntityId()).isEqualTo(ENTITY_ID.toString());
		}

		@Test
		@DisplayName("leaves entityId null when getId() cannot be resolved on the result")
		void leavesEntityIdNullWhenGetIdMissing() throws Throwable {
			Auditable meta = auditable(AuditAction.CREATE, AuditEntityType.DEVICE, "create device");
			when(joinPoint.getArgs()).thenReturn(new Object[0]);
			when(joinPoint.proceed()).thenReturn(new Object());

			auditAspect.audit(joinPoint, meta);

			verify(auditLogService).saveAuditLog(auditLogCaptor.capture());
			assertThat(auditLogCaptor.getValue().getEntityId()).isNull();
		}

		@Test
		@DisplayName("captures the request remote address as IP")
		void capturesRemoteAddress() throws Throwable {
			Auditable meta = auditable(AuditAction.READ, AuditEntityType.USER, "read user");
			setRequestWithRemoteAddr(MOCK_IP);
			when(joinPoint.getArgs()).thenReturn(new Object[0]);
			when(joinPoint.proceed()).thenReturn(new Object());

			auditAspect.audit(joinPoint, meta);

			verify(auditLogService).saveAuditLog(auditLogCaptor.capture());
			assertThat(auditLogCaptor.getValue().getIpAddress()).isEqualTo(MOCK_IP);
		}

		@Test
		@DisplayName("leaves IP null when there are no request attributes")
		void leavesIpNullWithoutRequestAttributes() throws Throwable {
			Auditable meta = auditable(AuditAction.READ, AuditEntityType.USER, "read user");
			when(joinPoint.getArgs()).thenReturn(new Object[0]);
			when(joinPoint.proceed()).thenReturn(new Object());

			auditAspect.audit(joinPoint, meta);

			verify(auditLogService).saveAuditLog(auditLogCaptor.capture());
			assertThat(auditLogCaptor.getValue().getIpAddress()).isNull();
		}
	}

	@Nested
	@DisplayName("Authentication details")
	class AuthenticationDetails {

		@Test
		@DisplayName("populates username and ROLE_ authorities from the security context")
		void populatesFromSecurityContext() throws Throwable {
			Auditable meta = auditable(AuditAction.UPDATE, AuditEntityType.DEPARTMENT, "update department");
			Authentication authentication = new UsernamePasswordAuthenticationToken(
					USERNAME,
					PASSWORD,
					List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("SCOPE_read")));
			setSecurityContext(authentication);
			when(joinPoint.getArgs()).thenReturn(new Object[0]);
			when(joinPoint.proceed()).thenReturn(new Object());

			auditAspect.audit(joinPoint, meta);

			verify(auditLogService).saveAuditLog(auditLogCaptor.capture());
			AuditLog log = auditLogCaptor.getValue();
			assertThat(log.getUsername()).isEqualTo(USERNAME);
			assertThat(log.getUserRoles()).isEqualTo("[\"ROLE_ADMIN\"]");
		}

		@Test
		@DisplayName("ignores anonymous authentication")
		void ignoresAnonymousUser() throws Throwable {
			Auditable meta = auditable(AuditAction.READ, AuditEntityType.USER, "read user");
			Authentication authentication = new UsernamePasswordAuthenticationToken(
					USERNAME_ANONYMOUS, PASSWORD_ANONYMOUS, List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
			setSecurityContext(authentication);
			when(joinPoint.getArgs()).thenReturn(new Object[0]);
			when(joinPoint.proceed()).thenReturn(new Object());

			auditAspect.audit(joinPoint, meta);

			verify(auditLogService).saveAuditLog(auditLogCaptor.capture());
			AuditLog log = auditLogCaptor.getValue();
			assertThat(log.getUsername()).isNull();
			assertThat(log.getUserRoles()).isNull();
		}
	}

	@Nested
	@DisplayName("LOGIN action")
	class LoginAction {

		@Test
		@DisplayName("takes the username from the LoginRequest argument")
		void takesUsernameFromLoginRequest() throws Throwable {
			Auditable meta = auditable(AuditAction.LOGIN, AuditEntityType.USER, "login");
			LoginRequest loginRequest = new LoginRequest(USERNAME, PASSWORD);
			when(joinPoint.getArgs()).thenReturn(new Object[]{loginRequest});
			when(joinPoint.proceed()).thenReturn(new Object());

			auditAspect.audit(joinPoint, meta);

			verify(auditLogService).saveAuditLog(auditLogCaptor.capture());
			assertThat(auditLogCaptor.getValue().getUsername()).isEqualTo(USERNAME);
		}

		@Test
		@DisplayName("enriches details from the returned Authentication on successful login")
		void enrichesDetailsFromAuthenticationResult() throws Throwable {
			Auditable meta = auditable(AuditAction.LOGIN, AuditEntityType.USER, "login");
			LoginRequest loginRequest = new LoginRequest(USERNAME, PASSWORD);
			Authentication result = new UsernamePasswordAuthenticationToken(
					USERNAME, PASSWORD, List.of(new SimpleGrantedAuthority("ROLE_OPERATOR")));
			when(joinPoint.getArgs()).thenReturn(new Object[]{loginRequest});
			when(joinPoint.proceed()).thenReturn(result);

			auditAspect.audit(joinPoint, meta);

			verify(auditLogService).saveAuditLog(auditLogCaptor.capture());
			AuditLog log = auditLogCaptor.getValue();
			assertThat(log.getUsername()).isEqualTo(USERNAME);
			assertThat(log.getUserRoles()).isEqualTo("[\"ROLE_OPERATOR\"]");
		}
	}

	@Nested
	@DisplayName("Failed invocation")
	class FailedInvocation {

		@Test
		@DisplayName("marks failure, records the error and rethrows the exception, still saving the log")
		void marksFailureAndRethrows() throws Throwable {
			Auditable meta = auditable(AuditAction.DELETE, AuditEntityType.ORGANIZATION, "delete organization");
			RuntimeException boom = new RuntimeException("error");
			when(joinPoint.getArgs()).thenReturn(new Object[0]);
			when(joinPoint.proceed()).thenThrow(boom);

			assertThatThrownBy(() -> auditAspect.audit(joinPoint, meta))
					.isSameAs(boom);

			verify(auditLogService).saveAuditLog(auditLogCaptor.capture());
			AuditLog log = auditLogCaptor.getValue();
			assertThat(log.isSuccess()).isFalse();
			assertThat(log.getErrorDetail()).isEqualTo("error");
		}

		@Test
		@DisplayName("saves the audit log exactly once")
		void savesAuditLogOnce() throws Throwable {
			Auditable meta = auditable(AuditAction.READ, AuditEntityType.DEVICE_TYPE, "read type");
			when(joinPoint.getArgs()).thenReturn(new Object[0]);
			when(joinPoint.proceed()).thenReturn(new Object());

			auditAspect.audit(joinPoint, meta);

			verify(auditLogService, times(1)).saveAuditLog(auditLogCaptor.capture());
		}
	}
}

