package com.gp.radioregistry.device.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.Set;

@Getter
@AllArgsConstructor
public enum DeviceStatus {
	ACTIVE,
	PENDING_INSTALLATION,
	UNDER_MAINTENANCE,
	OUT_OF_SERVICE,
	PENDING_DECOMMISSIONING,
	DECOMMISSIONED;

	private static final Map<DeviceStatus, Set<DeviceStatus>> ALLOWED_TRANSITIONS = Map.of(
		PENDING_INSTALLATION,    Set.of(ACTIVE),
		ACTIVE,                  Set.of(UNDER_MAINTENANCE, OUT_OF_SERVICE, PENDING_DECOMMISSIONING),
		UNDER_MAINTENANCE,       Set.of(ACTIVE, OUT_OF_SERVICE, PENDING_DECOMMISSIONING),
		OUT_OF_SERVICE,          Set.of(ACTIVE, UNDER_MAINTENANCE, PENDING_DECOMMISSIONING),
		PENDING_DECOMMISSIONING, Set.of(DECOMMISSIONED),
		DECOMMISSIONED,          Set.of()
	);

	public boolean canTransitionTo(DeviceStatus newStatus) {
		return ALLOWED_TRANSITIONS.get(this).contains(newStatus);
	}
}
