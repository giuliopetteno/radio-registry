package com.gp.radioregistry.controller;

import com.gp.radioregistry.domain.DeviceType;
import com.gp.radioregistry.request.CreateDeviceTypeRequest;
import com.gp.radioregistry.response.DeviceTypeResponse;
import com.gp.radioregistry.service.DeviceTypeService;
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
@RequestMapping("/device-types")
@Tag(name = "DeviceType controller", description = "API for managing device types")
public class DeviceTypeController {
    private final DeviceTypeService deviceTypeService;

    public DeviceTypeController(DeviceTypeService deviceTypeService) {
        this.deviceTypeService = deviceTypeService;
    }

    @PostMapping
    @Operation(summary = "Create a new device type", description = "Receives a new device type, validates it and saves it.")
    public ResponseEntity<DeviceTypeResponse> createDeviceType(@Valid @RequestBody CreateDeviceTypeRequest request) {
        log.info("Creation request received for device type with name: {}", request.name());

        DeviceType deviceType = deviceTypeService.createDeviceType(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(DeviceTypeResponse.fromEntity(deviceType));
    }
}

