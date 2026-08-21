package com.gp.radioregistry.device.repository;

import com.gp.radioregistry.device.domain.Device;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.hibernate.jpa.AvailableHints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@QueryHints(@QueryHint(name = AvailableHints.HINT_SPEC_LOCK_TIMEOUT, value = "3000"))
	@Query("SELECT d FROM Device d WHERE d.id = :id")
	Optional<Device> findByIdPessimisticLock(@Param("id") Long id);
}

