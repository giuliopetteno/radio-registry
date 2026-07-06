package com.gp.radioregistry.department.repository;

import com.gp.radioregistry.config.AbstractPostgresContainerTest;
import com.gp.radioregistry.department.domain.Department;
import com.gp.radioregistry.organization.domain.Organization;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class DepartmentRepositoryTest extends AbstractPostgresContainerTest {

    private static final String DEPARTMENT_NAME = "Radiology";
    private static final String DEPARTMENT_CODE = "RAD-1";
    private static final String DEPARTMENT_DESCRIPTION = "Radiology principal department";
    private static final String DEPARTMENT_NAME_SECONDARY = "Radiology secondary";
    private static final String DEPARTMENT_CODE_SECONDARY = "RAD-2";
    private static final String ORGANIZATION_NAME = "Sacred Heart Hospital";
    private static final String ORGANIZATION_CODE = "SCR-HRT";
    private static final String ORGANIZATION_DESCRIPTION = "Main headquarters";

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Organization persistOrganization() {
        var organization = Organization.builder()
                .name(ORGANIZATION_NAME)
                .code(ORGANIZATION_CODE)
                .description(ORGANIZATION_DESCRIPTION)
                .build();
        return entityManager.persistFlushFind(organization);
    }

    @Test
    void savePersistsDepartmentAndGeneratesId() {
        var organization = persistOrganization();

        var department = Department.builder()
                .name(DEPARTMENT_NAME)
                .code(DEPARTMENT_CODE)
                .description(DEPARTMENT_DESCRIPTION)
                .organization(organization)
                .build();

        var saved = departmentRepository.save(department);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByIdReturnsPersistedDepartment() {
        var organization = persistOrganization();

        var department = Department.builder()
                .name(DEPARTMENT_NAME)
                .code(DEPARTMENT_CODE)
                .description(DEPARTMENT_DESCRIPTION)
                .organization(organization)
                .build();
        var saved = departmentRepository.save(department);
        entityManager.flush();
        entityManager.clear();

        Optional<Department> found = departmentRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(DEPARTMENT_NAME);
        assertThat(found.get().getCode()).isEqualTo(DEPARTMENT_CODE);
        assertThat(found.get().getOrganization().getId()).isEqualTo(organization.getId());
    }

    @Test
    void findByIdReturnsEmptyWhenDepartmentDoesNotExist() {
        Optional<Department> found = departmentRepository.findById(-1L);

        assertThat(found).isEmpty();
    }

    @Test
    void findAllReturnsAllPersistedDepartments() {
        var organization = persistOrganization();

        departmentRepository.save(Department.builder()
                .name(DEPARTMENT_NAME)
                .code(DEPARTMENT_CODE)
                .description(DEPARTMENT_DESCRIPTION)
                .organization(organization)
                .build());
        departmentRepository.save(Department.builder()
                .name(DEPARTMENT_NAME_SECONDARY)
                .code(DEPARTMENT_CODE_SECONDARY)
                .organization(organization)
                .build());

        List<Department> departments = departmentRepository.findAll();

        assertThat(departments)
                .hasSize(2)
                .extracting(Department::getCode)
                .containsExactlyInAnyOrder(DEPARTMENT_CODE, DEPARTMENT_CODE_SECONDARY);
    }

    @Test
    void countReturnsNumberOfDepartments() {
        var organization = persistOrganization();

        departmentRepository.save(Department.builder()
                .name(DEPARTMENT_NAME)
                .code(DEPARTMENT_CODE)
                .organization(organization)
                .build());

        assertThat(departmentRepository.count()).isEqualTo(1);
    }

    @Test
    void deleteRemovesDepartment() {
        var organization = persistOrganization();

        var saved = departmentRepository.save(Department.builder()
                .name(DEPARTMENT_NAME)
                .code(DEPARTMENT_CODE)
                .organization(organization)
                .build());
        entityManager.flush();

        departmentRepository.delete(saved);
        entityManager.flush();

        assertThat(departmentRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void supportsSelfReferencingParentDepartment() {
        var organization = persistOrganization();

        var parent = departmentRepository.save(Department.builder()
                .name(DEPARTMENT_NAME)
                .code(DEPARTMENT_CODE)
                .organization(organization)
                .build());

        var child = departmentRepository.save(Department.builder()
                .name(DEPARTMENT_NAME_SECONDARY)
                .code(DEPARTMENT_CODE_SECONDARY)
                .organization(organization)
                .parentDepartment(parent)
                .build());
        entityManager.flush();
        entityManager.clear();

        var childFound = departmentRepository.findById(child.getId()).orElseThrow();

        assertThat(childFound.getParentDepartment()).isNotNull();
        assertThat(childFound.getParentDepartment().getId()).isEqualTo(parent.getId());
    }

    @Test
    void savingDepartmentWithoutOrganizationViolatesNotNullConstraint() {
        var department = Department.builder()
                .name(DEPARTMENT_NAME)
                .code(DEPARTMENT_CODE)
                .build();

        assertThatThrownBy(() -> {
            departmentRepository.save(department);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
