package com.gp.radioregistry.repository;

import com.gp.radioregistry.domain.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceTypeRepository extends JpaRepository<DeviceType, Long> {
}

