package com.gp.radioregistry.repository;

import com.gp.radioregistry.domain.Compartment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompartmentRepository extends JpaRepository<Compartment, Long> {
}

