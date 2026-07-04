package com.gp.radioregistry.devicetype.controller;

import com.gp.radioregistry.devicetype.dto.request.CreateDeviceTypeRequest;
import com.gp.radioregistry.devicetype.dto.request.UpdateDeviceTypeRequest;
import com.gp.radioregistry.devicetype.dto.response.DeviceTypeResponse;
import com.gp.radioregistry.devicetype.service.DeviceTypeService;
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

import static com.gp.radioregistry.constant.ApiConstants.DEVICE_TYPES_PATH;

@Slf4j
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

    @PutMapping("/{id}")
    @Operation(summary = "Update request for device type", description = "Updates a device type.")
    public ResponseEntity<DeviceTypeResponse> updateDeviceType(@PathVariable Long id, @Valid @RequestBody UpdateDeviceTypeRequest request) {
        log.info("Update request received for device type with id: {}", id);

        var deviceType = deviceTypeService.updateDeviceType(id, request);

        return ResponseEntity.ok(DeviceTypeResponse.fromEntity(deviceType));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete request for device type", description = "Deletes a device type by ID.")
    public ResponseEntity<Void> deleteDeviceType(@PathVariable Long id) {
        log.info("Delete request received for device type with id: {}", id);

        deviceTypeService.deleteDeviceType(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "List all device types", description = "Returns the complete list of device types available in the system.")
    public ResponseEntity<Page<DeviceTypeResponse>> getDeviceTypes(@ParameterObject Pageable pageable) {
        log.info("Request received to fetch all device types");

        var deviceTypes = deviceTypeService.getDeviceTypes(pageable);

        return ResponseEntity.ok(deviceTypes.map(DeviceTypeResponse::fromEntity));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get device type by id", description = "Returns a single device type matching the given id.")
    public ResponseEntity<DeviceTypeResponse> getDeviceTypeById(@PathVariable Long id) {
        log.info("Request received to fetch device type with id: {}", id);

        var deviceType = deviceTypeService.getDeviceTypeById(id);

        return ResponseEntity.ok(DeviceTypeResponse.fromEntity(deviceType));
    }
}

