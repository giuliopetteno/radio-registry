package com.gp.radioregistry.device.service;

import com.gp.radioregistry.audit.annotation.Auditable;
import com.gp.radioregistry.audit.enums.AuditAction;
import com.gp.radioregistry.audit.enums.AuditEntityType;
import com.gp.radioregistry.department.repository.DepartmentRepository;
import com.gp.radioregistry.device.domain.Device;
import com.gp.radioregistry.device.dto.request.CreateDeviceRequest;
import com.gp.radioregistry.device.dto.request.UpdateDeviceRequest;
import com.gp.radioregistry.device.repository.DeviceRepository;
import com.gp.radioregistry.devicetype.repository.DeviceTypeRepository;
import com.gp.radioregistry.organization.repository.OrganizationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final DeviceTypeRepository deviceTypeRepository;
    private final OrganizationRepository organizationRepository;
    private final DepartmentRepository departmentRepository;

    @Auditable(action = AuditAction.CREATE, entityType = AuditEntityType.DEVICE, entityId = "#result.id")
    public Device createDevice(CreateDeviceRequest request) {
        var device = Device.builder()
                .name(request.name())
                .deviceType(deviceTypeRepository.getReferenceById(request.deviceTypeId()))
                .serialNumber(request.serialNumber())
                .installationDate(request.installationDate())
                .description(request.description())
                .organization(request.organizationId() != null ? organizationRepository.getReferenceById(request.organizationId()) : null)
                .department(request.departmentId() != null ? departmentRepository.getReferenceById(request.departmentId()) : null)
                .build();
        return deviceRepository.save(device);
    }

    @Auditable(action = AuditAction.UPDATE, entityType = AuditEntityType.DEVICE, entityId = "#id")
    public Device updateDevice(Long id, UpdateDeviceRequest request) {
        var device = getDeviceById(id);
        Optional.ofNullable(request.name()).ifPresent(device::setName);
        Optional.ofNullable(request.deviceTypeId())
            .map(deviceTypeRepository::getReferenceById)
            .ifPresent(device::setDeviceType);
        Optional.ofNullable(request.serialNumber()).ifPresent(device::setSerialNumber);
        device.setDescription(request.description());
        Optional.ofNullable(request.installationDate()).ifPresent(device::setInstallationDate);
        device.setOrganization(request.organizationId() != null ? organizationRepository.getReferenceById(request.organizationId()) : null);
        device.setDepartment(request.departmentId() != null ? departmentRepository.getReferenceById(request.departmentId()) : null);

        return deviceRepository.save(device);
    }

    @Auditable(action = AuditAction.DELETE, entityType = AuditEntityType.DEVICE, entityId = "#id")
    public void deleteDevice(Long id) {
        var device = deviceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Device not found with id: " + id));
        deviceRepository.delete(device);
    }

    public Page<Device> getDevices(Pageable pageable) {
        return deviceRepository.findAll(pageable);
    }

    public Device getDeviceById(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Device not found with ID: " + id));
    }
}

