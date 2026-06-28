package com.gp.radioregistry.controller;


import com.gp.radioregistry.request.CreateDeviceRequest;
import com.gp.radioregistry.response.DeviceResponse;
import com.gp.radioregistry.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static com.gp.radioregistry.constant.AppConstants.Api.DEVICES_PATH;

@Log4j2
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

    @GetMapping
    @Operation(summary = "List all devices", description = "Returns the complete list of devices available in the system.")
    public ResponseEntity<List<DeviceResponse>> getDevices() {
        log.info("Request received to fetch all devices");

        var devices = deviceService.getDevices();

        return ResponseEntity.ok(devices.stream().map(DeviceResponse::fromEntity).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get device by id", description = "Returns a single device matching the given id.")
    public ResponseEntity<DeviceResponse> getDeviceById(@PathVariable Long id) {
        log.info("Request received to fetch device with id: {}", id);

        var device = deviceService.getDeviceById(id);

        return ResponseEntity.ok(DeviceResponse.fromEntity(device));
    }
}

