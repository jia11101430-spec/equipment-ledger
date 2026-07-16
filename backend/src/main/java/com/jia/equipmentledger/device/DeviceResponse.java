package com.jia.equipmentledger.device;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DeviceResponse(
        long id,
        String code,
        String name,
        String model,
        LocalDate factoryDate,
        BigDecimal purchasePrice,
        Long workshopId,
        String responsiblePerson,
        String status,
        String description
) {
}
