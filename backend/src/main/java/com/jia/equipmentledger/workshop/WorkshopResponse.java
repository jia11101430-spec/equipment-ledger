package com.jia.equipmentledger.workshop;

public record WorkshopResponse(
        Long id,
        String name,
        String managerName,
        String location,
        String phone,
        String description
) {
}