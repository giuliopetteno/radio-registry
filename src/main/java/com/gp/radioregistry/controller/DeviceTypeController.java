package com.gp.radioregistry.controller;

import com.gp.radioregistry.request.CreateDeviceTypeRequest;
import com.gp.radioregistry.response.DeviceTypeResponse;
import com.gp.radioregistry.service.DeviceTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static com.gp.radioregistry.constant.AppConstants.Api.DEVICE_TYPES_PATH;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping(DEVICE_TYPES_PATH)
@Tag(name = "DeviceTypes controller", description = "API for managing device types")
public class DeviceTypeController {
    private final DeviceTypeService deviceTypeService;

    @PostMapping
    @Operation(summary = "Create a new device type", description = "Receives a new device type, validates it and saves it.")
    public ResponseEntity<DeviceTypeResponse> createDeviceType(@Valid @RequestBody CreateDeviceTypeRequest request) {
        log.info("Creation request received for device type with name: {}", request.name());

        var deviceType = deviceTypeService.createDeviceType(request);

        return ResponseEntity.created(URI.create(String.format("%s/%d", DEVICE_TYPES_PATH, deviceType.getId()))).body(DeviceTypeResponse.fromEntity(deviceType));
    }

    @GetMapping
    @Operation(summary = "List all device types", description = "Returns the complete list of device types available in the system.")
    public ResponseEntity<List<DeviceTypeResponse>> getDeviceTypes() {
        log.info("Request received to fetch all device types");

        var deviceTypes = deviceTypeService.getDeviceTypes();

        return ResponseEntity.ok(deviceTypes.stream().map(DeviceTypeResponse::fromEntity).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get device type by id", description = "Returns a single device type matching the given id.")
    public ResponseEntity<DeviceTypeResponse> getDeviceTypeById(@PathVariable Long id) {
        log.info("Request received to fetch device type with id: {}", id);

        var deviceType = deviceTypeService.getDeviceTypeById(id);

        return ResponseEntity.ok(DeviceTypeResponse.fromEntity(deviceType));
    }
}

