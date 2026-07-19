package com.gp.radioregistry.device.repository;

import com.gp.radioregistry.base.AbstractPostgresContainerTest;
import com.gp.radioregistry.department.domain.Department;
import com.gp.radioregistry.department.repository.DepartmentRepository;
import com.gp.radioregistry.device.domain.Device;
import com.gp.radioregistry.device.enums.DeviceStatus;
import com.gp.radioregistry.devicetype.domain.DeviceType;
import com.gp.radioregistry.exception.InvalidEntityStateException;
import com.gp.radioregistry.organization.domain.Organization;
import com.gp.radioregistry.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class DeviceRepositoryTest extends AbstractPostgresContainerTest {

    private static final String DEVICE_NAME = "Portable X-Ray Unit";
    private static final String DEVICE_NAME_SECONDARY = "Ceiling X-Ray Unit";
    private static final String SERIAL_NUMBER = "SN-0001";
    private static final String SERIAL_NUMBER_SECONDARY = "SN-0002";
    private static final String DEVICE_DESCRIPTION = "Mobile radiology device";
    private static final DeviceStatus DEVICE_STATUS = DeviceStatus.ACTIVE;
    private static final LocalDate INSTALLATION_DATE = LocalDate.of(2024, 1, 15);

    private static final String DEVICE_TYPE_NAME = "RX";
    private static final String ORGANIZATION_NAME = "Sacred Heart Hospital";
    private static final String ORGANIZATION_CODE = "SCR-HRT";
    private static final String DEPARTMENT_NAME = "Radiology";
    private static final String DEPARTMENT_CODE = "RAD-1";

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void cleanUp() {
        organizationRepository.deleteAll();
        departmentRepository.deleteAll();
        deviceRepository.deleteAll();
    }

    private DeviceType persistDeviceType() {
        return entityManager.persistFlushFind(DeviceType.builder()
                .name(DEVICE_TYPE_NAME)
                .build());
    }

    private Organization persistOrganization() {
        return entityManager.persistFlushFind(Organization.builder()
                .name(ORGANIZATION_NAME)
                .code(ORGANIZATION_CODE)
                .build());
    }

    private Department persistDepartment(Organization organization) {
        return entityManager.persistFlushFind(Department.builder()
                .name(DEPARTMENT_NAME)
                .code(DEPARTMENT_CODE)
                .organization(organization)
                .build());
    }

    @Test
    @DisplayName("should persist device and generate id")
    void savePersistsDeviceAndGeneratesId() {
        var deviceType = persistDeviceType();
        var organization = persistOrganization();

        var device = Device.builder()
                .name(DEVICE_NAME)
                .deviceType(deviceType)
                .deviceStatus(DEVICE_STATUS)
                .serialNumber(SERIAL_NUMBER)
                .description(DEVICE_DESCRIPTION)
                .installationDate(INSTALLATION_DATE)
                .organization(organization)
                .build();

        var saved = deviceRepository.save(device);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("should return persisted device by id")
    void findByIdReturnsPersistedDevice() {
        var deviceType = persistDeviceType();
        var organization = persistOrganization();

        var saved = deviceRepository.save(Device.builder()
                .name(DEVICE_NAME)
                .deviceType(deviceType)
                .deviceStatus(DEVICE_STATUS)
                .serialNumber(SERIAL_NUMBER)
                .installationDate(INSTALLATION_DATE)
                .organization(organization)
                .build());
        entityManager.flush();
        entityManager.clear();

        Optional<Device> found = deviceRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(DEVICE_NAME);
        assertThat(found.get().getSerialNumber()).isEqualTo(SERIAL_NUMBER);
        assertThat(found.get().getDeviceType().getId()).isEqualTo(deviceType.getId());
    }

    @Test
    @DisplayName("should return empty when device does not exist")
    void findByIdReturnsEmptyWhenDeviceDoesNotExist() {
        Optional<Device> found = deviceRepository.findById(-1L);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("should return all persisted devices")
    void findAllReturnsAllPersistedDevices() {
        var deviceType = persistDeviceType();
        var organization = persistOrganization();
        var department = persistDepartment(organization);


        deviceRepository.save(Device.builder()
                .name(DEVICE_NAME)
                .deviceType(deviceType)
                .deviceStatus(DEVICE_STATUS)
                .serialNumber(SERIAL_NUMBER)
                .installationDate(INSTALLATION_DATE)
                .organization(organization)
                .build());
        deviceRepository.save(Device.builder()
                .name(DEVICE_NAME_SECONDARY)
                .deviceType(deviceType)
                .deviceStatus(DEVICE_STATUS)
                .serialNumber(SERIAL_NUMBER_SECONDARY)
                .installationDate(INSTALLATION_DATE)
                .department(department)
                .build());

        List<Device> devices = deviceRepository.findAll();

        assertThat(devices)
                .hasSize(2)
                .extracting(Device::getSerialNumber)
                .containsExactlyInAnyOrder(SERIAL_NUMBER, SERIAL_NUMBER_SECONDARY);
    }

    @Test
    @DisplayName("should return the number of devices")
    void countReturnsNumberOfDevices() {
        var deviceType = persistDeviceType();
        var organization = persistOrganization();

        deviceRepository.save(Device.builder()
                .name(DEVICE_NAME)
                .deviceType(deviceType)
                .deviceStatus(DEVICE_STATUS)
                .serialNumber(SERIAL_NUMBER)
                .installationDate(INSTALLATION_DATE)
                .organization(organization)
                .build());

        assertThat(deviceRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("should remove the device")
    void deleteRemovesDevice() {
        var deviceType = persistDeviceType();
        var organization = persistOrganization();
        var department = persistDepartment(organization);

        var saved = deviceRepository.save(Device.builder()
                .name(DEVICE_NAME)
                .deviceType(deviceType)
                .deviceStatus(DEVICE_STATUS)
                .serialNumber(SERIAL_NUMBER)
                .installationDate(INSTALLATION_DATE)
                .department(department)
                .build());
        entityManager.flush();

        deviceRepository.delete(saved);
        entityManager.flush();

        assertThat(deviceRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("should persist organization association")
    void persistsOrganizationAssociations() {
        var deviceType = persistDeviceType();
        var organization = persistOrganization();

        var saved = deviceRepository.save(Device.builder()
            .name(DEVICE_NAME)
            .deviceType(deviceType)
            .deviceStatus(DEVICE_STATUS)
            .serialNumber(SERIAL_NUMBER)
            .installationDate(INSTALLATION_DATE)
            .organization(organization)
            .build());
        entityManager.flush();
        entityManager.clear();

        var found = deviceRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getOrganization()).isNotNull();
        assertThat(found.getOrganization().getId()).isEqualTo(organization.getId());
    }

    @Test
    @DisplayName("should persist department association")
    void persistsDepartmentAssociation() {
        var deviceType = persistDeviceType();
        var organization = persistOrganization();
        var department = persistDepartment(organization);

        var saved = deviceRepository.save(Device.builder()
            .name(DEVICE_NAME)
            .deviceType(deviceType)
            .deviceStatus(DEVICE_STATUS)
            .serialNumber(SERIAL_NUMBER)
            .installationDate(INSTALLATION_DATE)
            .department(department)
            .build());
        entityManager.flush();
        entityManager.clear();

        var found = deviceRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getDepartment()).isNotNull();
        assertThat(found.getDepartment().getId()).isEqualTo(department.getId());
    }

    @Test
    @DisplayName("should violate not-null constraint when saving device without device type")
    void savingDeviceWithoutDeviceTypeViolatesNotNullConstraint() {
        var organization = persistOrganization();

        var device = Device.builder()
                .name(DEVICE_NAME)
                .deviceStatus(DEVICE_STATUS)
                .serialNumber(SERIAL_NUMBER)
                .installationDate(INSTALLATION_DATE)
                .organization(organization)
                .build();

        assertThatThrownBy(() -> {
            deviceRepository.save(device);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("should violate constraint when saving device with both organization or department")
    void savingDeviceWithBothOrganizationOrDepartmentViolatesConstraint() {
        var organization = persistOrganization();
        var department = persistDepartment(organization);

        assertThatThrownBy(() -> {
            deviceRepository.save(Device.builder()
                .name(DEVICE_NAME)
                .deviceStatus(DEVICE_STATUS)
                .serialNumber(SERIAL_NUMBER)
                .organization(organization)
                .department(department)
                .build());
            entityManager.flush();
            entityManager.clear();
        }).isInstanceOf(InvalidEntityStateException.class);
    }

    @Test
    @DisplayName("should violate constraint when saving device without organization or department")
    void savingDeviceWithoutOrganizationOrDepartmentViolatesConstraint() {
        assertThatThrownBy(() -> {
            deviceRepository.save(Device.builder()
                .name(DEVICE_NAME)
                .deviceStatus(DEVICE_STATUS)
                .serialNumber(SERIAL_NUMBER)
                .build());
            entityManager.flush();
            entityManager.clear();
        }).isInstanceOf(InvalidEntityStateException.class);
    }
}
