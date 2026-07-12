package com.jia.equipmentledger.workshop;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workshops")
public class WorkshopController {

    private final WorkshopService workshopService;

    public WorkshopController(WorkshopService workshopService) {
        this.workshopService = workshopService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkshopResponse createWorkshop(@RequestBody CreateWorkshopRequest request) {
        return workshopService.createWorkshop(request);
    }

    @GetMapping
    public List<WorkshopResponse> listWorkshops() {
        return workshopService.listWorkshops();
    }
}
