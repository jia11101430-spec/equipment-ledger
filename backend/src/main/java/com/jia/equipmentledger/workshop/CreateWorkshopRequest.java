package com.jia.equipmentledger.workshop;

public record CreateWorkshopRequest(
        String name,
        String managerName,
        String location,
        String phone,
        String description
) {
}
