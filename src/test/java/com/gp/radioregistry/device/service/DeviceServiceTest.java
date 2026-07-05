package com.gp.radioregistry.device.service;

import com.gp.radioregistry.department.domain.Department;
import com.gp.radioregistry.department.repository.DepartmentRepository;
import com.gp.radioregistry.device.domain.Device;
import com.gp.radioregistry.device.dto.request.CreateDeviceRequest;
import com.gp.radioregistry.device.dto.request.UpdateDeviceRequest;
import com.gp.radioregistry.device.repository.DeviceRepository;
import com.gp.radioregistry.devicetype.domain.DeviceType;
import com.gp.radioregistry.devicetype.repository.DeviceTypeRepository;
import com.gp.radioregistry.organization.domain.Organization;
import com.gp.radioregistry.organization.repository.OrganizationRepository;
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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeviceService unit tests")
class DeviceServiceTest {

    private static final Long DEVICE_ID = 1L;
    private static final Long DEVICE_ID_NOT_FOUND = 99L;
    private static final Long DEVICE_TYPE_ID = 5L;
    private static final Long ORGANIZATION_ID = 10L;
    private static final Long DEPARTMENT_ID = 20L;
    private static final LocalDate INSTALL_DATE = LocalDate.of(2024, 1, 15);

    private static final String DEVICE_NAME = "X-ray machine XR3600";
    private static final String DEVICE_SERIAL_NUMBER = "SN-XR3600";
    private static final String DEVICE_DESCRIPTION = "X-ray machine 1st generation";

    private static final String DEVICE_NAME_UPDATE = "X-ray machine XR3800";
    private static final String DEVICE_SERIAL_NUMBER_UPDATE = "SN-XR3800";
    private static final String DEVICE_DESCRIPTION_UPDATE = "X-ray machine 2nd generation";
    private static final Long DEVICE_TYPE_ID_UPDATE = 6L;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceTypeRepository deviceTypeRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DeviceService deviceService;

    private Device device;

    @BeforeEach
    void setUp() {
        device = Device.builder()
                .id(DEVICE_ID)
                .name(DEVICE_NAME)
                .serialNumber(DEVICE_SERIAL_NUMBER)
                .description(DEVICE_DESCRIPTION)
                .installationDate(INSTALL_DATE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("createDevice")
    class CreateDevice {

        @Test
        @DisplayName("should resolve device type and organization references, build and save")
        void createDevice_withOrganization() {
            var request = new CreateDeviceRequest(DEVICE_NAME, DEVICE_TYPE_ID, DEVICE_SERIAL_NUMBER,
                    DEVICE_DESCRIPTION, INSTALL_DATE, ORGANIZATION_ID, null);
            var deviceTypeRef = new DeviceType();
            var orgRef = new Organization();
            when(deviceTypeRepository.getReferenceById(DEVICE_TYPE_ID)).thenReturn(deviceTypeRef);
            when(organizationRepository.getReferenceById(ORGANIZATION_ID)).thenReturn(orgRef);
            when(deviceRepository.save(any(Device.class))).thenReturn(device);

            Device result = deviceService.createDevice(request);

            assertSame(device, result);
            ArgumentCaptor<Device> captor = ArgumentCaptor.forClass(Device.class);
            verify(deviceRepository).save(captor.capture());
            Device saved = captor.getValue();
            assertEquals(DEVICE_NAME, saved.getName());
            assertEquals(DEVICE_SERIAL_NUMBER, saved.getSerialNumber());
            assertSame(deviceTypeRef, saved.getDeviceType());
            assertSame(orgRef, saved.getOrganization());
            assertNull(saved.getDepartment());
            verify(departmentRepository, never()).getReferenceById(any());
        }

        @Test
        @DisplayName("should resolve department reference when departmentId is provided")
        void createDevice_withDepartment() {
            var request = new CreateDeviceRequest(DEVICE_NAME_UPDATE, DEVICE_TYPE_ID_UPDATE, DEVICE_SERIAL_NUMBER_UPDATE,
                    DEVICE_DESCRIPTION_UPDATE, INSTALL_DATE, null, DEPARTMENT_ID);
            when(deviceTypeRepository.getReferenceById(DEVICE_TYPE_ID_UPDATE)).thenReturn(new DeviceType());
            var deptRef = new Department();
            when(departmentRepository.getReferenceById(DEPARTMENT_ID)).thenReturn(deptRef);
            when(deviceRepository.save(any(Device.class))).thenReturn(device);

            deviceService.createDevice(request);

            ArgumentCaptor<Device> captor = ArgumentCaptor.forClass(Device.class);
            verify(deviceRepository).save(captor.capture());
            assertSame(deptRef, captor.getValue().getDepartment());
            assertNull(captor.getValue().getOrganization());
            verify(organizationRepository, never()).getReferenceById(any());
        }
    }

    @Nested
    @DisplayName("updateDevice")
    class UpdateDevice {

        @Test
        @DisplayName("should update fields, resolve references and save")
        void updateDevice_updatesAndSaves() {
            var request = new UpdateDeviceRequest(DEVICE_NAME_UPDATE, DEVICE_TYPE_ID_UPDATE, DEVICE_SERIAL_NUMBER_UPDATE,
                    DEVICE_DESCRIPTION_UPDATE, INSTALL_DATE, ORGANIZATION_ID, null);
            var deviceTypeRef = new DeviceType();
            var orgRef = new Organization();
            when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.of(device));
            when(deviceTypeRepository.getReferenceById(DEVICE_TYPE_ID_UPDATE)).thenReturn(deviceTypeRef);
            when(organizationRepository.getReferenceById(ORGANIZATION_ID)).thenReturn(orgRef);
            when(deviceRepository.save(device)).thenReturn(device);

            Device result = deviceService.updateDevice(DEVICE_ID, request);

            assertEquals(DEVICE_NAME_UPDATE , result.getName());
            assertEquals(DEVICE_SERIAL_NUMBER_UPDATE, result.getSerialNumber());
            assertEquals(DEVICE_DESCRIPTION_UPDATE, result.getDescription());
            assertSame(deviceTypeRef, result.getDeviceType());
            assertSame(orgRef, result.getOrganization());
            assertNull(result.getDepartment());
            verify(deviceRepository).save(device);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when device does not exist")
        void updateDevice_notFound() {
            var request = new UpdateDeviceRequest(DEVICE_NAME_UPDATE, DEVICE_TYPE_ID_UPDATE, DEVICE_SERIAL_NUMBER_UPDATE,
                    DEVICE_DESCRIPTION_UPDATE, INSTALL_DATE, ORGANIZATION_ID, null);
            when(deviceRepository.findById(DEVICE_ID_NOT_FOUND)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> deviceService.updateDevice(DEVICE_ID_NOT_FOUND, request));
            verify(deviceRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteDevice")
    class DeleteDevice {

        @Test
        @DisplayName("should delete the device when it exists")
        void deleteDevice_deletes() {
            when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.of(device));

            deviceService.deleteDevice(DEVICE_ID);

            verify(deviceRepository).delete(device);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when device does not exist")
        void deleteDevice_notFound() {
            when(deviceRepository.findById(DEVICE_ID_NOT_FOUND)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> deviceService.deleteDevice(DEVICE_ID_NOT_FOUND));
            verify(deviceRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("getDevices")
    class GetDevices {

        @Test
        @DisplayName("should return the page returned by the repository")
        void getDevices_returnsPage() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Device> page = new PageImpl<>(List.of(device), pageable, 1);
            when(deviceRepository.findAll(pageable)).thenReturn(page);

            Page<Device> result = deviceService.getDevices(pageable);

            assertEquals(1, result.getTotalElements());
            assertSame(device, result.getContent().getFirst());
        }
    }

    @Nested
    @DisplayName("getDeviceById")
    class GetDeviceById {

        @Test
        @DisplayName("should return the device when it exists")
        void getDeviceById_returns() {
            when(deviceRepository.findById(DEVICE_ID)).thenReturn(Optional.of(device));

            Device result = deviceService.getDeviceById(DEVICE_ID);

            assertSame(device, result);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when device does not exist")
        void getDeviceById_notFound() {
            when(deviceRepository.findById(DEVICE_ID_NOT_FOUND)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> deviceService.getDeviceById(DEVICE_ID_NOT_FOUND));
        }
    }
}

