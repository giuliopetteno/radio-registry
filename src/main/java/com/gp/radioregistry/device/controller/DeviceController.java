package com.gp.radioregistry.device.controller;


import com.gp.radioregistry.device.dto.request.CreateDeviceRequest;
import com.gp.radioregistry.device.dto.request.UpdateDeviceRequest;
import com.gp.radioregistry.device.dto.response.DeviceResponse;
import com.gp.radioregistry.device.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static com.gp.radioregistry.constant.ApiConstants.DEVICES_PATH;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(DEVICES_PATH)
@Tag(name = "Devices controller", description = "API for managing devices")
public class DeviceController {
    private final DeviceService deviceService;

    @PostMapping
    @Operation(summary = "Create a new device", description = "Receives a new device, validates it and saves it.")
    public ResponseEntity<DeviceResponse> createDevice(@Valid @RequestBody CreateDeviceRequest request) {
        log.info("Creation request received for device with name: {}", request.name());

        var device = deviceService.createDevice(request);

        return ResponseEntity.created(URI.create(String.format("%s/%d", DEVICES_PATH, device.getId()))).body(DeviceResponse.fromEntity(device));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update request for device", description = "Updates a device.")
    public ResponseEntity<DeviceResponse> updateDevice(@PathVariable Long id, @Valid @RequestBody UpdateDeviceRequest request) {
        log.info("Update request received for device with id: {}", id);

        var device = deviceService.updateDevice(id, request);

        return ResponseEntity.ok(DeviceResponse.fromEntity(device));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete request for device", description = "Deletes a device by ID.")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        log.info("Delete request received for device with id: {}", id);

        deviceService.deleteDevice(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "List all devices", description = "Returns the complete list of devices available in the system.")
    public ResponseEntity<Page<DeviceResponse>> getDevices(@ParameterObject Pageable pageable) {
        log.info("Request received to fetch all devices");

        var devices = deviceService.getDevices(pageable);

        return ResponseEntity.ok(devices.map(DeviceResponse::fromEntity));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get device by id", description = "Returns a single device matching the given id.")
    public ResponseEntity<DeviceResponse> getDeviceById(@PathVariable Long id) {
        log.info("Request received to fetch device with id: {}", id);

        var device = deviceService.getDeviceById(id);

        return ResponseEntity.ok(DeviceResponse.fromEntity(device));
    }
}

