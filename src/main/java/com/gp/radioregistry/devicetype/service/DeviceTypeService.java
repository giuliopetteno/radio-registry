package com.gp.radioregistry.devicetype.service;

import com.gp.radioregistry.audit.annotation.Auditable;
import com.gp.radioregistry.audit.enums.AuditAction;
import com.gp.radioregistry.audit.enums.AuditEntityType;
import com.gp.radioregistry.devicetype.domain.DeviceType;
import com.gp.radioregistry.devicetype.dto.request.CreateDeviceTypeRequest;
import com.gp.radioregistry.devicetype.dto.request.UpdateDeviceTypeRequest;
import com.gp.radioregistry.devicetype.repository.DeviceTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceTypeService {
    private final DeviceTypeRepository deviceTypeRepository;

    @Auditable(action = AuditAction.CREATE, entityType = AuditEntityType.DEVICE_TYPE, entityId = "#result.id")
    public DeviceType createDeviceType(CreateDeviceTypeRequest request) {
        var deviceType = DeviceType.builder()
                .name(request.name().trim())
                .description(request.description())
                .build();
        return deviceTypeRepository.save(deviceType);
    }

    @Auditable(action = AuditAction.UPDATE, entityType = AuditEntityType.DEVICE_TYPE, entityId = "#id")
    public DeviceType updateDeviceType(Long id, UpdateDeviceTypeRequest request) {
        var deviceType = getDeviceTypeById(id);
        Optional.ofNullable(request.name()).ifPresent(deviceType::setName);
        deviceType.setDescription(request.description());

        return deviceTypeRepository.save(deviceType);
    }

    @Auditable(action = AuditAction.DELETE, entityType = AuditEntityType.DEVICE_TYPE, entityId = "#id")
    public void deleteDeviceType(Long id) {
        var deviceType = deviceTypeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Device type not found with id: " + id));
        deviceTypeRepository.delete(deviceType);
    }

    public Page<DeviceType> getDeviceTypes(Pageable pageable) {
        return deviceTypeRepository.findAll(pageable);
    }

    public DeviceType getDeviceTypeById(Long id) {
        return deviceTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Device type not found with ID: " + id));
    }
}

