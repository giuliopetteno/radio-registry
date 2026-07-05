package com.gp.radioregistry.devicetype.controller;

import com.gp.radioregistry.devicetype.domain.DeviceType;
import com.gp.radioregistry.devicetype.dto.request.CreateDeviceTypeRequest;
import com.gp.radioregistry.devicetype.dto.request.UpdateDeviceTypeRequest;
import com.gp.radioregistry.devicetype.dto.response.DeviceTypeResponse;
import com.gp.radioregistry.devicetype.service.DeviceTypeService;
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
import java.time.OffsetDateTime;
import java.util.List;

import static com.gp.radioregistry.constant.ApiConstants.DEVICE_TYPES_PATH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeviceTypeController unit tests")
class DeviceTypeControllerTest {

    private static final Long DEVICE_TYPE_ID = 1L;
    private static final Long DEVICE_TYPE_ID_NOT_FOUND = 99L;
    private static final String DEVICE_TYPE_NAME = "CAT";
    private static final String DEVICE_TYPE_DESCRIPTION = "CAT machine";

    private static final String DEVICE_TYPE_NAME_UPDATE = "RX";
    private static final String DEVICE_TYPE_DESCRIPTION_UPDATE = "RX machine";

    @Mock
    private DeviceTypeService deviceTypeService;

    @InjectMocks
    private DeviceTypeController deviceTypeController;

    private DeviceType deviceType;

    @BeforeEach
    void setUp() {
        deviceType = DeviceType.builder()
                .id(DEVICE_TYPE_ID)
                .name(DEVICE_TYPE_NAME)
                .description(DEVICE_TYPE_DESCRIPTION)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("createDeviceType")
    class CreateDeviceType {

        @Test
        @DisplayName("should return 201 Created with location header and mapped body")
        void createDeviceType_returnsCreated() {
            var request = new CreateDeviceTypeRequest(DEVICE_TYPE_NAME, DEVICE_TYPE_DESCRIPTION);
            when(deviceTypeService.createDeviceType(request)).thenReturn(deviceType);

            ResponseEntity<DeviceTypeResponse> response = deviceTypeController.createDeviceType(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(URI.create(DEVICE_TYPES_PATH + "/" + DEVICE_TYPE_ID), response.getHeaders().getLocation());
            assertNotNull(response.getBody());
            assertEquals(DEVICE_TYPE_ID, response.getBody().id());
            assertEquals(DEVICE_TYPE_NAME, response.getBody().name());
            assertEquals(DEVICE_TYPE_DESCRIPTION, response.getBody().description());
            verify(deviceTypeService).createDeviceType(request);
        }
    }

    @Nested
    @DisplayName("updateDeviceType")
    class UpdateDeviceType {

        @Test
        @DisplayName("should return 200 OK with the updated device type")
        void updateDeviceType_returnsOk() {
            var request = new UpdateDeviceTypeRequest(DEVICE_TYPE_NAME_UPDATE, DEVICE_TYPE_DESCRIPTION_UPDATE);
            deviceType.setName(DEVICE_TYPE_NAME_UPDATE);
            deviceType.setDescription(DEVICE_TYPE_DESCRIPTION_UPDATE);
            when(deviceTypeService.updateDeviceType(DEVICE_TYPE_ID, request)).thenReturn(deviceType);

            ResponseEntity<DeviceTypeResponse> response = deviceTypeController.updateDeviceType(DEVICE_TYPE_ID, request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(DEVICE_TYPE_NAME_UPDATE, response.getBody().name());
            assertEquals(DEVICE_TYPE_DESCRIPTION_UPDATE, response.getBody().description());
            verify(deviceTypeService).updateDeviceType(DEVICE_TYPE_ID, request);
        }

        @Test
        @DisplayName("should propagate EntityNotFoundException when device type does not exist")
        void updateDeviceType_notFound() {
            var request = new UpdateDeviceTypeRequest(DEVICE_TYPE_NAME_UPDATE, DEVICE_TYPE_DESCRIPTION_UPDATE);
            when(deviceTypeService.updateDeviceType(anyLong(), any(UpdateDeviceTypeRequest.class)))
                    .thenThrow(new EntityNotFoundException("Device type not found"));

            assertThrows(EntityNotFoundException.class,
                    () -> deviceTypeController.updateDeviceType(DEVICE_TYPE_ID_NOT_FOUND, request));
        }
    }

    @Nested
    @DisplayName("deleteDeviceType")
    class DeleteDeviceType {

        @Test
        @DisplayName("should return 204 No Content and delegate to service")
        void deleteDeviceType_returnsNoContent() {
            ResponseEntity<Void> response = deviceTypeController.deleteDeviceType(DEVICE_TYPE_ID);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertNull(response.getBody());
            verify(deviceTypeService).deleteDeviceType(DEVICE_TYPE_ID);
        }

        @Test
        @DisplayName("should propagate EntityNotFoundException when device type does not exist")
        void deleteDeviceType_notFound() {
            doThrow(new EntityNotFoundException("Device type not found"))
                    .when(deviceTypeService).deleteDeviceType(DEVICE_TYPE_ID_NOT_FOUND);

            assertThrows(EntityNotFoundException.class, () -> deviceTypeController.deleteDeviceType(DEVICE_TYPE_ID_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("getDeviceTypes")
    class GetDeviceTypes {

        @Test
        @DisplayName("should return 200 OK with a mapped page of device types")
        void getDeviceTypes_returnsPage() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<DeviceType> page = new PageImpl<>(List.of(deviceType), pageable, 1);
            when(deviceTypeService.getDeviceTypes(pageable)).thenReturn(page);

            ResponseEntity<Page<DeviceTypeResponse>> response = deviceTypeController.getDeviceTypes(pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getTotalElements());
            assertEquals(DEVICE_TYPE_ID, response.getBody().getContent().getFirst().id());
            verify(deviceTypeService).getDeviceTypes(pageable);
        }

        @Test
        @DisplayName("should return 200 OK with an empty page when no device types exist")
        void getDeviceTypes_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 20);
            when(deviceTypeService.getDeviceTypes(pageable)).thenReturn(Page.empty(pageable));

            ResponseEntity<Page<DeviceTypeResponse>> response = deviceTypeController.getDeviceTypes(pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());
        }
    }

    @Nested
    @DisplayName("getDeviceTypeById")
    class GetDeviceTypeById {

        @Test
        @DisplayName("should return 200 OK with the mapped device type")
        void getDeviceTypeById_returnsOk() {
            when(deviceTypeService.getDeviceTypeById(DEVICE_TYPE_ID)).thenReturn(deviceType);

            ResponseEntity<DeviceTypeResponse> response = deviceTypeController.getDeviceTypeById(DEVICE_TYPE_ID);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(DEVICE_TYPE_ID, response.getBody().id());
            assertEquals(DEVICE_TYPE_NAME, response.getBody().name());
            assertEquals(DEVICE_TYPE_DESCRIPTION, response.getBody().description());
            verify(deviceTypeService).getDeviceTypeById(DEVICE_TYPE_ID);
        }

        @Test
        @DisplayName("should propagate EntityNotFoundException when device type does not exist")
        void getDeviceTypeById_notFound() {
            when(deviceTypeService.getDeviceTypeById(DEVICE_TYPE_ID_NOT_FOUND))
                    .thenThrow(new EntityNotFoundException("Device type not found"));

            assertThrows(EntityNotFoundException.class, () -> deviceTypeController.getDeviceTypeById(DEVICE_TYPE_ID_NOT_FOUND));
        }
    }
}

