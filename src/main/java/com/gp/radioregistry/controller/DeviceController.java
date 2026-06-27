package com.gp.radioregistry.controller;


import com.gp.radioregistry.request.CreateDeviceRequest;
import com.gp.radioregistry.response.DeviceResponse;
import com.gp.radioregistry.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/devices")
@Tag(name = "Device controller", description = "API for managing devices")
public class DeviceController {
    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping
    @Operation(summary = "Create a new device", description = "Receives a new device, validates it and saves it.")
    public ResponseEntity<DeviceResponse> createDevice(@Valid @RequestBody CreateDeviceRequest request) {
        log.info("Creation request received for device with name: {}", request.name());

        var device = deviceService.createDevice(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(DeviceResponse.fromEntity(device));
    }
}

