package com.gp.radioregistry.device;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeviceStatus {
	PENDING_INSTALLATION,
	ACTIVE,
	MAINTENANCE,
	OUT_OF_SERVICE,
	PENDING_DECOMMISSIONING,
	DECOMMISSIONED
}
