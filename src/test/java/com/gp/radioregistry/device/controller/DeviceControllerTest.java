package com.gp.radioregistry.device.controller;

import com.gp.radioregistry.device.domain.Device;
import com.gp.radioregistry.device.dto.request.CreateDeviceRequest;
import com.gp.radioregistry.device.dto.request.UpdateDeviceRequest;
import com.gp.radioregistry.device.dto.response.DeviceResponse;
import com.gp.radioregistry.device.service.DeviceService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static com.gp.radioregistry.constant.ApiConstants.DEVICES_PATH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeviceController unit tests")
class DeviceControllerTest {

    private static final Long DEVICE_ID = 1L;
    private static final Long DEVICE_ID_NOT_FOUND = 99L;
    private static final Long DEVICE_TYPE_ID = 5L;
    private static final Long ORGANIZATION_ID = 10L;
    private static final String DEVICE_NAME = "X-ray machine XR3600";
    private static final String DEVICE_SERIAL_NUMBER = "SN-XR3600";
    private static final String DEVICE_DESCRIPTION = "X-ray machine 1st generation";

    private static final String DEVICE_NAME_UPDATE = "X-ray machine XR3800";
    private static final String DEVICE_SERIAL_NUMBER_UPDATE = "SN-XR3800";
    private static final String DEVICE_DESCRIPTION_UPDATE = "X-ray machine 2nd generation";

    @Mock
    private DeviceService deviceService;

    @InjectMocks
    private DeviceController deviceController;

    private Device device;

    @BeforeEach
    void setUp() {
        device = Device.builder()
                .id(DEVICE_ID)
                .name(DEVICE_NAME)
                .serialNumber(DEVICE_SERIAL_NUMBER)
                .description(DEVICE_DESCRIPTION)
                .installationDate(LocalDate.of(2024, 1, 15))
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    private CreateDeviceRequest createRequest() {
        return new CreateDeviceRequest(DEVICE_NAME, DEVICE_TYPE_ID, DEVICE_SERIAL_NUMBER, DEVICE_DESCRIPTION,
                LocalDate.of(2024, 1, 15), ORGANIZATION_ID, null);
    }

    private UpdateDeviceRequest updateRequest() {
        return new UpdateDeviceRequest(DEVICE_NAME_UPDATE, DEVICE_TYPE_ID, DEVICE_SERIAL_NUMBER_UPDATE, DEVICE_DESCRIPTION_UPDATE,
                LocalDate.of(2024, 1, 15), ORGANIZATION_ID, null);
    }

    @Nested
    @DisplayName("createDevice")
    class CreateDevice {

        @Test
        @DisplayName("should return 201 Created with location header and mapped body")
        void createDevice_returnsCreated() {
            var request = createRequest();
            when(deviceService.createDevice(request)).thenReturn(device);

            ResponseEntity<DeviceResponse> response = deviceController.createDevice(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(URI.create(DEVICES_PATH + "/" + DEVICE_ID), response.getHeaders().getLocation());
            assertNotNull(response.getBody());
            assertEquals(DEVICE_ID, response.getBody().id());
            assertEquals(DEVICE_NAME, response.getBody().name());
            assertEquals(DEVICE_SERIAL_NUMBER, response.getBody().serialNumber());
            assertEquals(DEVICE_DESCRIPTION, response.getBody().description());
            verify(deviceService).createDevice(request);
        }
    }

    @Nested
    @DisplayName("updateDevice")
    class UpdateDevice {

        @Test
        @DisplayName("should return 200 OK with the updated device")
        void updateDevice_returnsOk() {
            var request = updateRequest();
            device.setName(DEVICE_NAME_UPDATE);
            device.setDescription(DEVICE_DESCRIPTION_UPDATE);
            when(deviceService.updateDevice(DEVICE_ID, request)).thenReturn(device);

            ResponseEntity<DeviceResponse> response = deviceController.updateDevice(DEVICE_ID, request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(DEVICE_NAME_UPDATE, response.getBody().name());
            assertEquals(DEVICE_DESCRIPTION_UPDATE, response.getBody().description());
            verify(deviceService).updateDevice(DEVICE_ID, request);
        }

        @Test
        @DisplayName("should propagate EntityNotFoundException when device does not exist")
        void updateDevice_notFound() {
            var request = updateRequest();
            when(deviceService.updateDevice(anyLong(), request))
                    .thenThrow(new EntityNotFoundException("Device not found"));

            assertThrows(EntityNotFoundException.class, () -> deviceController.updateDevice(DEVICE_ID_NOT_FOUND, request));
        }
    }

    @Nested
    @DisplayName("deleteDevice")
    class DeleteDevice {

        @Test
        @DisplayName("should return 204 No Content and delegate to service")
        void deleteDevice_returnsNoContent() {
            ResponseEntity<Void> response = deviceController.deleteDevice(DEVICE_ID);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertNull(response.getBody());
            verify(deviceService).deleteDevice(DEVICE_ID);
        }

        @Test
        @DisplayName("should propagate EntityNotFoundException when device does not exist")
        void deleteDevice_notFound() {
            doThrow(new EntityNotFoundException("Device not found")).when(deviceService).deleteDevice(DEVICE_ID_NOT_FOUND);

            assertThrows(EntityNotFoundException.class, () -> deviceController.deleteDevice(DEVICE_ID_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("getDevices")
    class GetDevices {

        @Test
        @DisplayName("should return 200 OK with a mapped page of devices")
        void getDevices_returnsPage() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Device> page = new PageImpl<>(List.of(device), pageable, 1);
            when(deviceService.getDevices(pageable)).thenReturn(page);

            ResponseEntity<Page<DeviceResponse>> response = deviceController.getDevices(pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getTotalElements());
            assertEquals(DEVICE_ID, response.getBody().getContent().getFirst().id());
            verify(deviceService).getDevices(pageable);
        }

        @Test
        @DisplayName("should return 200 OK with an empty page when no devices exist")
        void getDevices_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 20);
            when(deviceService.getDevices(pageable)).thenReturn(Page.empty(pageable));

            ResponseEntity<Page<DeviceResponse>> response = deviceController.getDevices(pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());
        }
    }

    @Nested
    @DisplayName("getDeviceById")
    class GetDeviceById {

        @Test
        @DisplayName("should return 200 OK with the mapped device")
        void getDeviceById_returnsOk() {
            when(deviceService.getDeviceById(DEVICE_ID)).thenReturn(device);

            ResponseEntity<DeviceResponse> response = deviceController.getDeviceById(DEVICE_ID);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(DEVICE_ID, response.getBody().id());
            assertEquals(DEVICE_NAME, response.getBody().name());
            assertEquals(DEVICE_SERIAL_NUMBER, response.getBody().serialNumber());
            assertEquals(DEVICE_DESCRIPTION, response.getBody().description());
            verify(deviceService).getDeviceById(DEVICE_ID);
        }

        @Test
        @DisplayName("should propagate EntityNotFoundException when device does not exist")
        void getDeviceById_notFound() {
            when(deviceService.getDeviceById(DEVICE_ID_NOT_FOUND)).thenThrow(new EntityNotFoundException("Device not found"));

            assertThrows(EntityNotFoundException.class, () -> deviceController.getDeviceById(DEVICE_ID_NOT_FOUND));
        }
    }
}

