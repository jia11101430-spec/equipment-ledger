package com.jia.equipmentledger.workshop;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkshopService {

    private final WorkshopRepository workshopRepository;

    public WorkshopService(WorkshopRepository workshopRepository) {
        this.workshopRepository = workshopRepository;
    }

    @Transactional
    public WorkshopResponse createWorkshop(CreateWorkshopRequest request) {
        if (request == null || request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("车间名称不能为空");
        }

        CreateWorkshopRequest normalizedRequest = new CreateWorkshopRequest(
                request.name().trim(),
                request.managerName(),
                request.location(),
                request.phone(),
                request.description()
        );
        return workshopRepository.create(normalizedRequest);
    }

    @Transactional(readOnly = true)
    public List<WorkshopResponse> listWorkshops() {
        return workshopRepository.findAll();
    }
}
