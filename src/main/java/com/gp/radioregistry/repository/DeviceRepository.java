package com.gp.radioregistry.repository;

import com.gp.radioregistry.domain.Device;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, Long> {
}

