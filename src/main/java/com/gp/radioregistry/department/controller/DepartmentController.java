package com.gp.radioregistry.department.controller;


import com.gp.radioregistry.department.dto.request.CreateDepartmentRequest;
import com.gp.radioregistry.department.dto.request.UpdateDepartmentRequest;
import com.gp.radioregistry.department.dto.response.DepartmentResponse;
import com.gp.radioregistry.department.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static com.gp.radioregistry.constant.ApiConstants.DEPARTMENTS_PATH;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(DEPARTMENTS_PATH)
@Tag(name = "Departments controller", description = "API for managing departments")
public class DepartmentController {
    private final DepartmentService departmentService;

    @PostMapping
    @Operation(summary = "Create a new department", description = "Receives a new department, validates it and saves it.")
    public ResponseEntity<DepartmentResponse> createDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        log.info("Creation request received for department with name: {}", request.name());

        var department = departmentService.createDepartment(request);

        return ResponseEntity.created(URI.create(String.format("%s/%d", DEPARTMENTS_PATH, department.getId()))).body(DepartmentResponse.fromEntity(department));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update request for department", description = "Updates a department.")
    public ResponseEntity<DepartmentResponse> updateDepartment(@PathVariable Long id, @Valid @RequestBody UpdateDepartmentRequest request) {
        log.info("Update request received for department with id: {}", id);

        var department = departmentService.updateDepartment(id, request);

        return ResponseEntity.ok(DepartmentResponse.fromEntity(department));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete request for department", description = "Deletes a department by ID.")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        log.info("Delete request received for department with id: {}", id);

        departmentService.deleteDepartment(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "List all departments", description = "Returns the complete list of departments available in the system.")
    public ResponseEntity<List<DepartmentResponse>> getDepartments() {
        log.info("Request received to fetch all departments");

        var departments = departmentService.getDepartments();

        return ResponseEntity.ok(departments.stream().map(DepartmentResponse::fromEntity).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get department by id", description = "Returns a single department matching the given id.")
    public ResponseEntity<DepartmentResponse> getDepartmentById(@PathVariable Long id) {
        log.info("Request received to fetch department with id: {}", id);

        var department = departmentService.getDepartmentById(id);

        return ResponseEntity.ok(DepartmentResponse.fromEntity(department));
    }
}

