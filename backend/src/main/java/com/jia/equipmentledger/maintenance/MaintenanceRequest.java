package com.jia.equipmentledger.maintenance;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MaintenanceRequest(
        Long deviceId,
        LocalDateTime maintenanceTime,
        String faultType,
        String faultDescription,
        String repairDescription,
        BigDecimal maintenanceCost,
        String operatorName
) {
}
