package com.jia.equipmentledger.maintenance;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;

    public MaintenanceService(MaintenanceRepository maintenanceRepository) {
        this.maintenanceRepository = maintenanceRepository;
    }

    @Transactional
    public MaintenanceResponse create(MaintenanceRequest request) {
        if (request == null || request.deviceId() == null) {
            throw new IllegalArgumentException("设备 ID 不能为空");
        }
        if (request.maintenanceTime() == null) {
            throw new IllegalArgumentException("维修时间不能为空");
        }
        if (!maintenanceRepository.deviceExists(request.deviceId())) {
            throw new IllegalArgumentException("设备不存在");
        }
        BigDecimal cost = request.maintenanceCost() == null ? BigDecimal.ZERO : request.maintenanceCost();
        if (cost.signum() < 0) {
            throw new IllegalArgumentException("维修费用不能为负数");
        }
        return maintenanceRepository.create(new MaintenanceRequest(
                request.deviceId(), request.maintenanceTime(), request.faultType(),
                request.faultDescription(), request.repairDescription(), cost, request.operatorName()));
    }

    @Transactional(readOnly = true)
    public List<MaintenanceResponse> list() {
        return maintenanceRepository.findAll();
    }
}
