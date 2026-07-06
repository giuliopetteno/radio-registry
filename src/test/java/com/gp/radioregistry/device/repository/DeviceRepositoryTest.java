package com.gp.radioregistry.device.repository;

import com.gp.radioregistry.config.AbstractPostgresContainerTest;
import com.gp.radioregistry.department.domain.Department;
import com.gp.radioregistry.device.domain.Device;
import com.gp.radioregistry.devicetype.domain.DeviceType;
import com.gp.radioregistry.organization.domain.Organization;
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
    private static final LocalDate INSTALLATION_DATE = LocalDate.of(2024, 1, 15);

    private static final String DEVICE_TYPE_NAME = "X-Ray Machine";
    private static final String ORGANIZATION_NAME = "Sacred Heart Hospital";
    private static final String ORGANIZATION_CODE = "SCR-HRT";
    private static final String DEPARTMENT_NAME = "Radiology";
    private static final String DEPARTMENT_CODE = "RAD-1";

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private TestEntityManager entityManager;

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
    void savePersistsDeviceAndGeneratesId() {
        var deviceType = persistDeviceType();

        var device = Device.builder()
                .name(DEVICE_NAME)
                .deviceType(deviceType)
                .serialNumber(SERIAL_NUMBER)
                .description(DEVICE_DESCRIPTION)
                .installationDate(INSTALLATION_DATE)
                .build();

        var saved = deviceRepository.save(device);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByIdReturnsPersistedDevice() {
        var deviceType = persistDeviceType();

        var saved = deviceRepository.save(Device.builder()
                .name(DEVICE_NAME)
                .deviceType(deviceType)
                .serialNumber(SERIAL_NUMBER)
                .installationDate(INSTALLATION_DATE)
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
    void findByIdReturnsEmptyWhenDeviceDoesNotExist() {
        Optional<Device> found = deviceRepository.findById(-1L);

        assertThat(found).isEmpty();
    }

    @Test
    void findAllReturnsAllPersistedDevices() {
        var deviceType = persistDeviceType();

        deviceRepository.save(Device.builder()
                .name(DEVICE_NAME)
                .deviceType(deviceType)
                .serialNumber(SERIAL_NUMBER)
                .installationDate(INSTALLATION_DATE)
                .build());
        deviceRepository.save(Device.builder()
                .name(DEVICE_NAME_SECONDARY)
                .deviceType(deviceType)
                .serialNumber(SERIAL_NUMBER_SECONDARY)
                .installationDate(INSTALLATION_DATE)
                .build());

        List<Device> devices = deviceRepository.findAll();

        assertThat(devices)
                .hasSize(2)
                .extracting(Device::getSerialNumber)
                .containsExactlyInAnyOrder(SERIAL_NUMBER, SERIAL_NUMBER_SECONDARY);
    }

    @Test
    void countReturnsNumberOfDevices() {
        var deviceType = persistDeviceType();

        deviceRepository.save(Device.builder()
                .name(DEVICE_NAME)
                .deviceType(deviceType)
                .serialNumber(SERIAL_NUMBER)
                .installationDate(INSTALLATION_DATE)
                .build());

        assertThat(deviceRepository.count()).isEqualTo(1);
    }

    @Test
    void deleteRemovesDevice() {
        var deviceType = persistDeviceType();

        var saved = deviceRepository.save(Device.builder()
                .name(DEVICE_NAME)
                .deviceType(deviceType)
                .serialNumber(SERIAL_NUMBER)
                .installationDate(INSTALLATION_DATE)
                .build());
        entityManager.flush();

        deviceRepository.delete(saved);
        entityManager.flush();

        assertThat(deviceRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void persistsOrganizationAndDepartmentAssociations() {
        var deviceType = persistDeviceType();
        var organization = persistOrganization();
        var department = persistDepartment(organization);

        var saved = deviceRepository.save(Device.builder()
                .name(DEVICE_NAME)
                .deviceType(deviceType)
                .serialNumber(SERIAL_NUMBER)
                .installationDate(INSTALLATION_DATE)
                .organization(organization)
                .department(department)
                .build());
        entityManager.flush();
        entityManager.clear();

        var found = deviceRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getOrganization()).isNotNull();
        assertThat(found.getOrganization().getId()).isEqualTo(organization.getId());
        assertThat(found.getDepartment()).isNotNull();
        assertThat(found.getDepartment().getId()).isEqualTo(department.getId());
    }

    @Test
    void savingDeviceWithoutDeviceTypeViolatesNotNullConstraint() {
        var device = Device.builder()
                .name(DEVICE_NAME)
                .serialNumber(SERIAL_NUMBER)
                .installationDate(INSTALLATION_DATE)
                .build();

        assertThatThrownBy(() -> {
            deviceRepository.save(device);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}

