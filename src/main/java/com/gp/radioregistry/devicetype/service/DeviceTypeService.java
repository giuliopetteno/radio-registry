package com.gp.radioregistry.devicetype.service;

import com.gp.radioregistry.devicetype.domain.DeviceType;
import com.gp.radioregistry.devicetype.repository.DeviceTypeRepository;
import com.gp.radioregistry.devicetype.dto.request.CreateDeviceTypeRequest;
import com.gp.radioregistry.devicetype.dto.request.UpdateDeviceTypeRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceTypeService {
    private final DeviceTypeRepository deviceTypeRepository;

    public DeviceType createDeviceType(CreateDeviceTypeRequest request) {
        var deviceType = DeviceType.builder()
                .name(request.name().trim())
                .description(request.description())
                .build();
        return deviceTypeRepository.save(deviceType);
    }

    public DeviceType updateDeviceType(Long id, UpdateDeviceTypeRequest request) {
        var deviceType = getDeviceTypeById(id);
        Optional.ofNullable(request.name()).ifPresent(deviceType::setName);
        deviceType.setDescription(request.description());

        return deviceTypeRepository.save(deviceType);
    }

    public void deleteDeviceType(Long id) {
        var deviceType = deviceTypeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Device type not found with id: " + id));
        deviceTypeRepository.delete(deviceType);
    }

    public List<DeviceType> getDeviceTypes() {

        return deviceTypeRepository.findAll();
    }

    public DeviceType getDeviceTypeById(Long id) {
        return deviceTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Device type not found with ID: " + id));
    }
}

