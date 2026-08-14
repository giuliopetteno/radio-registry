package com.gp.radioregistry.device.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeviceStatus {
	ACTIVE,
	PENDING_INSTALLATION,
	UNDER_MAINTENANCE,
	OUT_OF_SERVICE,
	PENDING_DECOMMISSIONING,
	DECOMMISSIONED
}
