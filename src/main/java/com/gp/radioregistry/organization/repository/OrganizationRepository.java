package com.gp.radioregistry.organization.repository;

import com.gp.radioregistry.organization.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
}

