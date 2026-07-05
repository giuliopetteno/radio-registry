package com.gp.radioregistry.devicetype.service;

import com.gp.radioregistry.devicetype.domain.DeviceType;
import com.gp.radioregistry.devicetype.dto.request.CreateDeviceTypeRequest;
import com.gp.radioregistry.devicetype.dto.request.UpdateDeviceTypeRequest;
import com.gp.radioregistry.devicetype.repository.DeviceTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeviceTypeService unit tests")
class DeviceTypeServiceTest {

    private static final Long DEVICE_TYPE_ID = 1L;

    @Mock
    private DeviceTypeRepository deviceTypeRepository;

    @InjectMocks
    private DeviceTypeService deviceTypeService;

    private DeviceType deviceType;

    @BeforeEach
    void setUp() {
        deviceType = DeviceType.builder()
                .id(DEVICE_TYPE_ID)
                .name("Handheld Radio")
                .description("Portable handheld radio")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("createDeviceType")
    class CreateDeviceType {

        @Test
        @DisplayName("should trim the name, build and save the device type")
        void createDeviceType_savesTrimmedName() {
            var request = new CreateDeviceTypeRequest("  Handheld Radio  ", "Portable handheld radio");
            when(deviceTypeRepository.save(any(DeviceType.class))).thenReturn(deviceType);

            DeviceType result = deviceTypeService.createDeviceType(request);

            assertSame(deviceType, result);
            ArgumentCaptor<DeviceType> captor = ArgumentCaptor.forClass(DeviceType.class);
            verify(deviceTypeRepository).save(captor.capture());
            assertEquals("Handheld Radio", captor.getValue().getName());
            assertEquals("Portable handheld radio", captor.getValue().getDescription());
        }
    }

    @Nested
    @DisplayName("updateDeviceType")
    class UpdateDeviceType {

        @Test
        @DisplayName("should update fields and save")
        void updateDeviceType_updatesAndSaves() {
            var request = new UpdateDeviceTypeRequest("New Name", "New description");
            when(deviceTypeRepository.findById(DEVICE_TYPE_ID)).thenReturn(Optional.of(deviceType));
            when(deviceTypeRepository.save(deviceType)).thenReturn(deviceType);

            DeviceType result = deviceTypeService.updateDeviceType(DEVICE_TYPE_ID, request);

            assertEquals("New Name", result.getName());
            assertEquals("New description", result.getDescription());
            verify(deviceTypeRepository).save(deviceType);
        }

        @Test
        @DisplayName("should keep existing name when request name is null")
        void updateDeviceType_keepsNameWhenNull() {
            var request = new UpdateDeviceTypeRequest(null, "New description");
            when(deviceTypeRepository.findById(DEVICE_TYPE_ID)).thenReturn(Optional.of(deviceType));
            when(deviceTypeRepository.save(deviceType)).thenReturn(deviceType);

            DeviceType result = deviceTypeService.updateDeviceType(DEVICE_TYPE_ID, request);

            assertEquals("Handheld Radio", result.getName());
            assertEquals("New description", result.getDescription());
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when device type does not exist")
        void updateDeviceType_notFound() {
            var request = new UpdateDeviceTypeRequest("New Name", null);
            when(deviceTypeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> deviceTypeService.updateDeviceType(99L, request));
            verify(deviceTypeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteDeviceType")
    class DeleteDeviceType {

        @Test
        @DisplayName("should delete the device type when it exists")
        void deleteDeviceType_deletes() {
            when(deviceTypeRepository.findById(DEVICE_TYPE_ID)).thenReturn(Optional.of(deviceType));

            deviceTypeService.deleteDeviceType(DEVICE_TYPE_ID);

            verify(deviceTypeRepository).delete(deviceType);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when device type does not exist")
        void deleteDeviceType_notFound() {
            when(deviceTypeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> deviceTypeService.deleteDeviceType(99L));
            verify(deviceTypeRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("getDeviceTypes")
    class GetDeviceTypes {

        @Test
        @DisplayName("should return the page returned by the repository")
        void getDeviceTypes_returnsPage() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<DeviceType> page = new PageImpl<>(List.of(deviceType), pageable, 1);
            when(deviceTypeRepository.findAll(pageable)).thenReturn(page);

            Page<DeviceType> result = deviceTypeService.getDeviceTypes(pageable);

            assertEquals(1, result.getTotalElements());
            assertSame(deviceType, result.getContent().getFirst());
        }
    }

    @Nested
    @DisplayName("getDeviceTypeById")
    class GetDeviceTypeById {

        @Test
        @DisplayName("should return the device type when it exists")
        void getDeviceTypeById_returns() {
            when(deviceTypeRepository.findById(DEVICE_TYPE_ID)).thenReturn(Optional.of(deviceType));

            DeviceType result = deviceTypeService.getDeviceTypeById(DEVICE_TYPE_ID);

            assertSame(deviceType, result);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when device type does not exist")
        void getDeviceTypeById_notFound() {
            when(deviceTypeRepository.findById(DEVICE_TYPE_ID)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> deviceTypeService.getDeviceTypeById(DEVICE_TYPE_ID));
        }
    }
}

