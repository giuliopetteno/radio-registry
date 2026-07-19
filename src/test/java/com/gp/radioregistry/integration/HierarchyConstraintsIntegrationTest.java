package com.gp.radioregistry.integration;

import com.gp.radioregistry.base.AbstractPostgresContainerTest;
import com.gp.radioregistry.role.domain.Role;
import com.gp.radioregistry.role.repository.RoleRepository;
import com.gp.radioregistry.user.domain.User;
import com.gp.radioregistry.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.gp.radioregistry.constant.ApiConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class HierarchyConstraintsIntegrationTest extends AbstractPostgresContainerTest {

    private static final String ADMIN_USERNAME = "ADMIN";
    private static final String ADMIN_PASSWORD = "Xk9#mQ2$vL7pT4nR";
    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String ORGANIZATION_NAME = "San Joseph Hospital";
    private static final String ORGANIZATION_CODE = "SAN-JSP";
    private static final String DEPARTMENT_NAME = "Radiology";
    private static final String DEPARTMENT_CODE = "RAD-1";
    private static final String DEPARTMENT_NAME_INVALID = "Radiology invalid";
    private static final String DEPARTMENT_CODE_INVALID = "RAD-1 invalid";
    private static final String DEPARTMENT_CHILD_NAME = "Radiology 2";
    private static final String DEPARTMENT_CHILD_CODE = "RAD-2";
    private static final String DEVICE_NAME = "A CAT machine";
    private static final String DEVICE_TYPE_SERIAL_NUMBER = "CAT34234";
    private static final String DEVICE_NAME_INVALID = "A CAT machine invalid";
    private static final String DEVICE_TYPE_SERIAL_NUMBER_INVALID = "CAT34234 invalid";
    private static final String DEVICE_TYPE_NAME = "CAT";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void seedAdminUser() {

        if (!userRepository.existsByUsername(ADMIN_USERNAME)) {
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("ADMIN").build()));

            User admin = User.builder()
                    .username(ADMIN_USERNAME)
                    .email(ADMIN_EMAIL)
                    .password(passwordEncoder.encode(ADMIN_PASSWORD))
                    .enabled(true)
                    .accountNonLocked(true)
                    .roles(Set.of(adminRole))
                    .build();
            userRepository.save(admin);
        }
    }

    @Test
    @DisplayName("Full valid hierarchy: Organization -> Department -> Device persists and links correctly")
    void fullValidHierarchy_isPersistedAndLinked() {
        Long orgId = createOrganization(ORGANIZATION_NAME, ORGANIZATION_CODE);

        Long deptId = createDepartment(DEPARTMENT_NAME, DEPARTMENT_CODE, orgId, null);
        Long deviceTypeId = createDeviceType(DEVICE_TYPE_NAME);
        Long deviceId = createDevice(DEVICE_NAME, deviceTypeId, DEVICE_TYPE_SERIAL_NUMBER, null, deptId);

        Map<String, Object> dept = getJson(DEPARTMENTS_PATH + "/" + deptId);
        assertThat(number(dept.get("organizationId"))).isEqualTo(orgId);
        assertThat(dept.get("parentDepartmentId")).isNull();

        Map<String, Object> device = getJson(DEVICES_PATH + "/" + deviceId);
        assertThat(number(device.get("departmentId"))).isEqualTo(deptId);
        assertThat(device.get("organizationId")).isNull();

        Map<String, Object> tree = getJson(ORGANIZATIONS_PATH + "/" + orgId + "/tree");
        assertThat(number(tree.get("id"))).isEqualTo(orgId);
    }

    @Test
    @DisplayName("Device attached directly to an Organization (no Department) is valid")
    void deviceAttachedDirectlyToOrganization_isValid() {
        Long orgId = createOrganization(ORGANIZATION_NAME, ORGANIZATION_CODE);
        Long deviceTypeId = createDeviceType(DEVICE_TYPE_NAME);

        Long deviceId = createDevice(DEVICE_NAME, deviceTypeId, DEVICE_TYPE_SERIAL_NUMBER, orgId, null);

        Map<String, Object> device = getJson(DEVICES_PATH + "/" + deviceId);
        assertThat(number(device.get("organizationId"))).isEqualTo(orgId);
        assertThat(device.get("departmentId")).isNull();
    }

    @Nested
    @DisplayName("Department XOR rule")
    class DepartmentXor {

        @Test
        @DisplayName("Department with BOTH organizationId and parentDepartmentId is rejected (400)")
        void department_withBothParents_isRejected() {
            Long orgId = createOrganization(ORGANIZATION_NAME, ORGANIZATION_CODE);
            Long parentDeptId = createDepartment(DEPARTMENT_NAME, DEPARTMENT_CODE, orgId, null);

            Map<String, Object> body = Map.of(
                    "name", DEPARTMENT_NAME_INVALID,
                    "code", DEPARTMENT_CODE_INVALID,
                    "organizationId", orgId,
                    "parentDepartmentId", parentDeptId);

            ResponseEntity<String> response = post(DEPARTMENTS_PATH, body, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Department with NEITHER organizationId nor parentDepartmentId is rejected (400)")
        void department_withNoParent_isRejected() {
            Map<String, Object> body = Map.of(
                    "name", DEPARTMENT_NAME_INVALID,
                    "code", DEPARTMENT_CODE_INVALID);

            ResponseEntity<String> response = post(DEPARTMENTS_PATH, body, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("Device XOR rule")
    class DeviceXor {

        @Test
        @DisplayName("Device with BOTH organizationId and departmentId is rejected (400)")
        void device_withBothParents_isRejected() {
            Long orgId = createOrganization(ORGANIZATION_NAME, ORGANIZATION_CODE);
            Long deptId = createDepartment(DEPARTMENT_NAME, DEPARTMENT_CODE, orgId, null);
            Long deviceTypeId = createDeviceType(DEVICE_TYPE_NAME);

            Map<String, Object> body = Map.of(
                    "name", DEVICE_NAME_INVALID,
                    "deviceTypeId", deviceTypeId,
                    "serialNumber", DEVICE_TYPE_SERIAL_NUMBER_INVALID,
                    "installationDate", LocalDate.now().toString(),
                    "organizationId", orgId,
                    "departmentId", deptId);

            ResponseEntity<String> response = post(DEVICES_PATH, body, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Device with NEITHER organizationId nor departmentId is rejected (400)")
        void device_withNoParent_isRejected() {
            Long deviceTypeId = createDeviceType(DEVICE_TYPE_NAME);

            Map<String, Object> body = Map.of(
                    "name", DEVICE_NAME_INVALID,
                    "deviceTypeId", deviceTypeId,
                    "serialNumber", DEVICE_TYPE_SERIAL_NUMBER_INVALID,
                    "installationDate", LocalDate.now().toString());

            ResponseEntity<String> response = post(DEVICES_PATH, body, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Test
    @DisplayName("DELETE Organization with linked Departments fails (FK RESTRICT).")
    void deleteOrganization_withDepartments_failsWithFkRestrict() {
        Long orgId = createOrganization(ORGANIZATION_NAME, ORGANIZATION_CODE);
        createDepartment(DEPARTMENT_NAME, DEPARTMENT_CODE, orgId, null);

        ResponseEntity<String> response = delete(ORGANIZATIONS_PATH + "/" + orgId);

        assertThat(response.getStatusCode())
                .as("Delete must be blocked by the FK RESTRICT constraint")
                .isNotEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getStatusCode().is5xxServerError())
                .as("DataIntegrityViolationException is unhandled -> 500")
                .isTrue();

        assertThat(get(ORGANIZATIONS_PATH + "/" + orgId + "/tree", String.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("DELETE parent Department with a child: ON DELETE SET NULL nulls the child's "
            + "parent_department_id, violating the XOR CHECK -> DB error, transaction rolls back.")
    void deleteParentDepartment_withChild_violatesXorCheckViaSetNull() {
        Long orgId = createOrganization(ORGANIZATION_NAME, ORGANIZATION_CODE);
        Long parentDeptId = createDepartment(DEPARTMENT_NAME, DEPARTMENT_CODE, orgId, null);
        Long childDeptId = createDepartment(DEPARTMENT_CHILD_NAME, DEPARTMENT_CHILD_CODE, null, parentDeptId);

        ResponseEntity<String> response = delete(DEPARTMENTS_PATH + "/" + parentDeptId);

        assertThat(response.getStatusCode())
                .as("Delete must not succeed: SET NULL would leave the child violating the XOR CHECK")
                .isNotEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getStatusCode().is5xxServerError())
                .as("The XOR CHECK violation surfaces as an unhandled DataIntegrityViolationException -> 500")
                .isTrue();

        Map<String, Object> parent = getJson(DEPARTMENTS_PATH + "/" + parentDeptId);
        assertThat(parent)
                .as("Parent must still exist after the rolled-back delete")
                .isNotNull();

        Map<String, Object> child = getJson(DEPARTMENTS_PATH + "/" + childDeptId);
        assertThat(number(child.get("parentDepartmentId")))
                .as("Child must still point to its parent after the rolled-back delete")
                .isEqualTo(parentDeptId);
    }

    @Test
    @DisplayName("DELETE DeviceType in use by a Device fails (FK RESTRICT).")
    void deleteDeviceType_inUse_failsWithFkRestrict() {
        Long orgId = createOrganization(ORGANIZATION_NAME, ORGANIZATION_CODE);
        Long deviceTypeId = createDeviceType(DEVICE_TYPE_NAME);
        createDevice("Device Eight", deviceTypeId, "SN-0008", orgId, null);

        ResponseEntity<String> response = delete(DEVICE_TYPES_PATH + "/" + deviceTypeId);

        assertThat(response.getStatusCode())
                .as("Delete must be blocked by the FK RESTRICT constraint")
                .isNotEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getStatusCode().is5xxServerError())
                .as("DataIntegrityViolationException is unhandled -> 500")
                .isTrue();

        assertThat(get(DEVICE_TYPES_PATH + "/" + deviceTypeId, String.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    private Long createOrganization(String name, String code) {
        Map<String, Object> body = Map.of("name", name, "code", code);
        Map<String, Object> created = postExpectingCreated(ORGANIZATIONS_PATH, body);
        return number(created.get("id"));
    }

    private Long createDepartment(String name, String code, Long organizationId, Long parentDepartmentId) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("code", code);
        if (organizationId != null) {
            body.put("organizationId", organizationId);
        }
        if (parentDepartmentId != null) {
            body.put("parentDepartmentId", parentDepartmentId);
        }
        Map<String, Object> created = postExpectingCreated(DEPARTMENTS_PATH, body);
        return number(created.get("id"));
    }

    private Long createDeviceType(String name) {
        Map<String, Object> body = Map.of("name", name);
        Map<String, Object> created = postExpectingCreated(DEVICE_TYPES_PATH, body);
        return number(created.get("id"));
    }

    private Long createDevice(String name, Long deviceTypeId, String serialNumber, Long organizationId, Long departmentId) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("deviceTypeId", deviceTypeId);
        body.put("serialNumber", serialNumber);
        body.put("installationDate", LocalDate.now().toString());
        if (organizationId != null) {
            body.put("organizationId", organizationId);
        }
        if (departmentId != null) {
            body.put("departmentId", departmentId);
        }
        Map<String, Object> created = postExpectingCreated(DEVICES_PATH, body);
        return number(created.get("id"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> postExpectingCreated(String path, Object body) {
        ResponseEntity<Map> response = post(path, body, Map.class);
        assertThat(response.getStatusCode())
                .as("Creation of %s should succeed", path)
                .isEqualTo(HttpStatus.CREATED);
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).as("Response body for %s must not be null", path).isNotNull();
        return responseBody;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getJson(String path) {
        ResponseEntity<Map> response = get(path, Map.class);
        assertThat(response.getStatusCode()).as("GET %s should return 200", path).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = response.getBody();
        assertThat(body).as("Response body for GET %s must not be null", path).isNotNull();
        return body;
    }

    private <T> ResponseEntity<T> post(String path, Object body, Class<T> responseType) {
        return authenticated().postForEntity(url(path), new HttpEntity<>(body, jsonHeaders()), responseType);
    }

    private <T> ResponseEntity<T> get(String path, Class<T> responseType) {
        return authenticated().getForEntity(url(path), responseType);
    }

    private ResponseEntity<String> delete(String path) {
        return authenticated().exchange(
                url(path), HttpMethod.DELETE, new HttpEntity<>(jsonHeaders()), String.class);
    }

    private TestRestTemplate authenticated() {
        return restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String url(String path) {
        return String.format("http://localhost:%d/api/v1%s", port, path);
    }

    private static Long number(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }
}
