package com.gp.radioregistry.repository;

import com.gp.radioregistry.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
}

