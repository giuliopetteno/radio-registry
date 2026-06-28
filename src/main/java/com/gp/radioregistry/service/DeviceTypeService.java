package com.gp.radioregistry.service;

import com.gp.radioregistry.domain.DeviceType;
import com.gp.radioregistry.repository.DeviceTypeRepository;
import com.gp.radioregistry.request.CreateDeviceTypeRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<DeviceType> getDeviceTypes() {

        return deviceTypeRepository.findAll();
    }

    public DeviceType getDeviceTypeById(Long id) {
        return deviceTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Device type not found with ID: " + id));
    }
}

