package com.gp.radioregistry.devicetype.service;

import com.gp.radioregistry.audit.annotation.Auditable;
import com.gp.radioregistry.devicetype.domain.DeviceType;
import com.gp.radioregistry.devicetype.dto.request.CreateDeviceTypeRequest;
import com.gp.radioregistry.devicetype.dto.request.UpdateDeviceTypeRequest;
import com.gp.radioregistry.devicetype.repository.DeviceTypeRepository;
import com.gp.radioregistry.enums.EntityType;
import com.gp.radioregistry.enums.EventType;
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

    @Auditable(eventType = EventType.CREATE, entityType = EntityType.DEVICE_TYPE, entityId = "#result.id", description = "Device type creation attempt")
    public DeviceType createDeviceType(CreateDeviceTypeRequest request) {
        var deviceType = DeviceType.builder()
                .name(request.name().trim())
                .description(request.description())
                .build();
        return deviceTypeRepository.save(deviceType);
    }

    @Auditable(eventType = EventType.UPDATE, entityType = EntityType.DEVICE_TYPE, entityId = "#id", description = "Device type update attempt")
    public DeviceType updateDeviceType(Long id, UpdateDeviceTypeRequest request) {
        var deviceType = getDeviceTypeById(id);
        Optional.ofNullable(request.name()).ifPresent(deviceType::setName);
        deviceType.setDescription(request.description());

        return deviceTypeRepository.save(deviceType);
    }

    @Auditable(eventType = EventType.DELETE, entityType = EntityType.DEVICE_TYPE, entityId = "#id", description = "Device type deletion attempt")
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

