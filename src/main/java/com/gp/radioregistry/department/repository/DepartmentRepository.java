package com.gp.radioregistry.department.repository;

import com.gp.radioregistry.department.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
	@Query(value = """
    WITH RECURSIVE parent_departments AS (
        SELECT id, parent_department_id
        FROM radio_registry.department
        WHERE id = :parentDepartmentId
        UNION ALL
        SELECT dept.id, dept.parent_department_id
        FROM radio_registry.department dept
        JOIN parent_departments pdept ON dept.id = pdept.parent_department_id
    )
    SELECT EXISTS (SELECT 1 FROM parent_departments WHERE id = :departmentId)
    """, nativeQuery = true)
	boolean isCreatingCycle(@Param("departmentId") Long departmentId, @Param("parentDepartmentId") Long parentDepartmentId);
}