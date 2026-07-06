package com.gp.radioregistry.devicetype.repository;

import com.gp.radioregistry.config.AbstractPostgresContainerTest;
import com.gp.radioregistry.devicetype.domain.DeviceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class DeviceTypeRepositoryTest extends AbstractPostgresContainerTest {

    private static final String DEVICE_TYPE_NAME = "X-Ray Machine";
    private static final String DEVICE_TYPE_DESCRIPTION = "Diagnostic imaging device";
    private static final String DEVICE_TYPE_NAME_SECONDARY = "Ultrasound Scanner";

    @Autowired
    private DeviceTypeRepository deviceTypeRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void savePersistsDeviceTypeAndGeneratesId() {
        var deviceType = DeviceType.builder()
                .name(DEVICE_TYPE_NAME)
                .description(DEVICE_TYPE_DESCRIPTION)
                .build();

        var saved = deviceTypeRepository.save(deviceType);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByIdReturnsPersistedDeviceType() {
        var saved = deviceTypeRepository.save(DeviceType.builder()
                .name(DEVICE_TYPE_NAME)
                .description(DEVICE_TYPE_DESCRIPTION)
                .build());
        entityManager.flush();
        entityManager.clear();

        Optional<DeviceType> found = deviceTypeRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(DEVICE_TYPE_NAME);
        assertThat(found.get().getDescription()).isEqualTo(DEVICE_TYPE_DESCRIPTION);
    }

    @Test
    void findByIdReturnsEmptyWhenDeviceTypeDoesNotExist() {
        Optional<DeviceType> found = deviceTypeRepository.findById(-1L);

        assertThat(found).isEmpty();
    }

    @Test
    void findAllReturnsAllPersistedDeviceTypes() {
        deviceTypeRepository.save(DeviceType.builder()
                .name(DEVICE_TYPE_NAME)
                .build());
        deviceTypeRepository.save(DeviceType.builder()
                .name(DEVICE_TYPE_NAME_SECONDARY)
                .build());

        List<DeviceType> deviceTypes = deviceTypeRepository.findAll();

        assertThat(deviceTypes)
                .hasSize(2)
                .extracting(DeviceType::getName)
                .containsExactlyInAnyOrder(DEVICE_TYPE_NAME, DEVICE_TYPE_NAME_SECONDARY);
    }

    @Test
    void countReturnsNumberOfDeviceTypes() {
        deviceTypeRepository.save(DeviceType.builder()
                .name(DEVICE_TYPE_NAME)
                .build());

        assertThat(deviceTypeRepository.count()).isEqualTo(1);
    }

    @Test
    void deleteRemovesDeviceType() {
        var saved = deviceTypeRepository.save(DeviceType.builder()
                .name(DEVICE_TYPE_NAME)
                .build());
        entityManager.flush();

        deviceTypeRepository.delete(saved);
        entityManager.flush();

        assertThat(deviceTypeRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void savingDeviceTypeWithoutNameViolatesNotNullConstraint() {
        var deviceType = DeviceType.builder()
                .description(DEVICE_TYPE_DESCRIPTION)
                .build();

        assertThatThrownBy(() -> {
            deviceTypeRepository.save(deviceType);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}

