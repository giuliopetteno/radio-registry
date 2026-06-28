package com.gp.radioregistry.service;

import com.gp.radioregistry.domain.Device;
import com.gp.radioregistry.repository.CompartmentRepository;
import com.gp.radioregistry.repository.DeviceRepository;
import com.gp.radioregistry.repository.DeviceTypeRepository;
import com.gp.radioregistry.repository.OrganizationRepository;
import com.gp.radioregistry.request.CreateDeviceRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final DeviceTypeRepository deviceTypeRepository;
    private final OrganizationRepository organizationRepository;
    private final CompartmentRepository compartmentRepository;

    public Device createDevice(CreateDeviceRequest request) {
        var device = Device.builder()
                .name(request.name())
                .deviceType(deviceTypeRepository.getReferenceById(request.deviceTypeId()))
                .serialNumber(request.serialNumber())
                .installationDate(request.installationDate())
                .description(request.description())
                .organization(request.organizationId() != null ? organizationRepository.getReferenceById(request.organizationId()) : null)
                .compartment(request.compartmentId() != null ? compartmentRepository.getReferenceById(request.compartmentId()) : null)
                .build();
        return deviceRepository.save(device);
    }

    public List<Device> getDevices() {
        return deviceRepository.findAll();
    }

    public Device getDeviceById(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Device not found with ID: " + id));
    }
}

