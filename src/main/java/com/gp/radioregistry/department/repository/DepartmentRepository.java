package com.gp.radioregistry.department.repository;

import com.gp.radioregistry.department.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}

