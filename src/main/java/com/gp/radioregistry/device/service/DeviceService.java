package com.gp.radioregistry.device.service;

import com.gp.radioregistry.audit.annotation.Auditable;
import com.gp.radioregistry.department.repository.DepartmentRepository;
import com.gp.radioregistry.device.domain.Device;
import com.gp.radioregistry.device.dto.request.CreateDeviceRequest;
import com.gp.radioregistry.device.dto.request.UpdateDeviceRequest;
import com.gp.radioregistry.device.repository.DeviceRepository;
import com.gp.radioregistry.devicetype.repository.DeviceTypeRepository;
import com.gp.radioregistry.enums.EntityType;
import com.gp.radioregistry.enums.EventType;
import com.gp.radioregistry.kafka.event.DeviceEvent;
import com.gp.radioregistry.kafka.outboxevent.service.OutboxEventService;
import com.gp.radioregistry.organization.repository.OrganizationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final DeviceTypeRepository deviceTypeRepository;
    private final OrganizationRepository organizationRepository;
    private final DepartmentRepository departmentRepository;
    private final OutboxEventService outboxEventService;

    @Transactional
    @Auditable(eventType = EventType.CREATE, entityType = EntityType.DEVICE, entityId = "#result.id", description = "Device creation attempt")
    public Device createDevice(CreateDeviceRequest request) {
        var device = Device.builder()
                .name(request.name())
                .deviceType(deviceTypeRepository.getReferenceById(request.deviceTypeId()))
                .serialNumber(request.serialNumber())
                .description(request.description())
                .installationDate(request.installationDate())
                .deviceStatus(request.deviceStatus())
                .decommissionDate(request.decommissionDate())
                .organization(request.organizationId() != null ? organizationRepository.getReferenceById(request.organizationId()) : null)
                .department(request.departmentId() != null ? departmentRepository.getReferenceById(request.departmentId()) : null)
                .build();

        var result = deviceRepository.save(device);
        saveDeviceOutboxEvent(EventType.CREATE, result);

        return result;
    }

    @Transactional
    @Auditable(eventType = EventType.UPDATE, entityType = EntityType.DEVICE, entityId = "#id", description = "Device update attempt")
    public Device updateDevice(Long id, UpdateDeviceRequest request) {
        var device = this.getDeviceById(id);
        boolean statusChanged = device.getDeviceStatus() != request.deviceStatus();

        Optional.ofNullable(request.name()).ifPresent(device::setName);
        Optional.ofNullable(request.deviceTypeId())
            .map(deviceTypeRepository::getReferenceById)
            .ifPresent(device::setDeviceType);
        Optional.ofNullable(request.serialNumber()).ifPresent(device::setSerialNumber);
        device.setDescription(request.description());
        Optional.ofNullable(request.installationDate()).ifPresent(device::setInstallationDate);
        Optional.ofNullable(request.deviceStatus()).ifPresent(device::setDeviceStatus);
        device.setDecommissionDate(request.decommissionDate());
        device.setOrganization(request.organizationId() != null ? organizationRepository.getReferenceById(request.organizationId()) : null);
        device.setDepartment(request.departmentId() != null ? departmentRepository.getReferenceById(request.departmentId()) : null);

        var result = deviceRepository.save(device);
        saveDeviceOutboxEvent(statusChanged ? EventType.STATUS_CHANGED : EventType.UPDATE, result);

        return result;
    }

    @Transactional
    @Auditable(eventType = EventType.DELETE, entityType = EntityType.DEVICE, entityId = "#id", description = "Device deletion attempt")
    public void deleteDevice(Long id) {
        var device = deviceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Device not found with id: " + id));

        saveDeviceOutboxEvent(EventType.DELETE, device);
        deviceRepository.delete(device);
    }

    public Page<Device> getDevices(Pageable pageable) {
        return deviceRepository.findAll(pageable);
    }

    public Device getDeviceById(Long id) {
        return deviceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Device not found with id: " + id));
    }

    private void saveDeviceOutboxEvent(EventType eventType, Device device) {
        outboxEventService.save(
            EntityType.DEVICE.name(),
            String.valueOf(device.getId()),
            eventType.name(),
            DeviceEvent.of(eventType, device)
        );
    }
}

