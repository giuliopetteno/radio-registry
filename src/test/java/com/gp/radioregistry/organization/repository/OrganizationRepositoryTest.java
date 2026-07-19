package com.gp.radioregistry.organization.repository;

import com.gp.radioregistry.base.AbstractPostgresContainerTest;
import com.gp.radioregistry.department.repository.DepartmentRepository;
import com.gp.radioregistry.organization.domain.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
class OrganizationRepositoryTest extends AbstractPostgresContainerTest {

    private static final String ORGANIZATION_NAME = "Sacred Heart Hospital";
    private static final String ORGANIZATION_CODE = "SCR-HRT";
    private static final String ORGANIZATION_DESCRIPTION = "Main headquarters";
    private static final String ORGANIZATION_NAME_SECONDARY = "Saint Vincent Clinic";
    private static final String ORGANIZATION_CODE_SECONDARY = "ST-VCT";

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void cleanUp() {
        departmentRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Test
    @DisplayName("should persist organization and generate id")
    void savePersistsOrganizationAndGeneratesId() {
        var organization = Organization.builder()
                .name(ORGANIZATION_NAME)
                .code(ORGANIZATION_CODE)
                .description(ORGANIZATION_DESCRIPTION)
                .build();

        var saved = organizationRepository.save(organization);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("should return persisted organization by id")
    void findByIdReturnsPersistedOrganization() {
        var saved = organizationRepository.save(Organization.builder()
                .name(ORGANIZATION_NAME)
                .code(ORGANIZATION_CODE)
                .description(ORGANIZATION_DESCRIPTION)
                .build());
        entityManager.flush();
        entityManager.clear();

        Optional<Organization> found = organizationRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(ORGANIZATION_NAME);
        assertThat(found.get().getCode()).isEqualTo(ORGANIZATION_CODE);
    }

    @Test
    @DisplayName("should return empty when organization does not exist")
    void findByIdReturnsEmptyWhenOrganizationDoesNotExist() {
        Optional<Organization> found = organizationRepository.findById(-1L);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("should return all persisted organizations")
    void findAllReturnsAllPersistedOrganizations() {
        organizationRepository.save(Organization.builder()
                .name(ORGANIZATION_NAME)
                .code(ORGANIZATION_CODE)
                .build());
        organizationRepository.save(Organization.builder()
                .name(ORGANIZATION_NAME_SECONDARY)
                .code(ORGANIZATION_CODE_SECONDARY)
                .build());

        List<Organization> organizations = organizationRepository.findAll();

        assertThat(organizations)
                .hasSize(2)
                .extracting(Organization::getCode)
                .containsExactlyInAnyOrder(ORGANIZATION_CODE, ORGANIZATION_CODE_SECONDARY);
    }

    @Test
    @DisplayName("should return the number of organizations")
    void countReturnsNumberOfOrganizations() {
        organizationRepository.save(Organization.builder()
                .name(ORGANIZATION_NAME)
                .code(ORGANIZATION_CODE)
                .build());

        assertThat(organizationRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("should remove the organization")
    void deleteRemovesOrganization() {
        var saved = organizationRepository.save(Organization.builder()
                .name(ORGANIZATION_NAME)
                .code(ORGANIZATION_CODE)
                .build());
        entityManager.flush();

        organizationRepository.delete(saved);
        entityManager.flush();

        assertThat(organizationRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("should violate not-null constraint when saving organization without name")
    void savingOrganizationWithoutNameViolatesNotNullConstraint() {
        var organization = Organization.builder()
                .code(ORGANIZATION_CODE)
                .build();

        assertThatThrownBy(() -> {
            organizationRepository.save(organization);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
