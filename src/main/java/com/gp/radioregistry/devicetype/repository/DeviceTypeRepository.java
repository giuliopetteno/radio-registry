package com.gp.radioregistry.devicetype.repository;

import com.gp.radioregistry.devicetype.domain.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceTypeRepository extends JpaRepository<DeviceType, Long> {
}

