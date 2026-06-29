package com.gp.radioregistry.device.repository;

import com.gp.radioregistry.device.domain.Device;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, Long> {
}

