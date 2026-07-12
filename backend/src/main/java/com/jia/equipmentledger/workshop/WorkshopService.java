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

    @Transactional(readOnly = true)
    public List<WorkshopResponse> listWorkshops() {
        return workshopRepository.findAll();
    }
}