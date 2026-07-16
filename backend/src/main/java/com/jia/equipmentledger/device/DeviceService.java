package com.jia.equipmentledger.device;

import java.util.List;
import java.math.BigDecimal;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> listDevices() {
        return deviceRepository.findAll();
    }

    @Transactional
    public DeviceResponse createDevice(DeviceRequest request) {
        DeviceRequest normalized = validateAndNormalize(request);
        validateWorkshop(normalized.workshopId());
        return deviceRepository.create(normalized);
    }

    @Transactional
    public DeviceResponse updateDevice(long id, DeviceRequest request) {
        DeviceRequest normalized = validateAndNormalize(request);
        validateWorkshop(normalized.workshopId());
        DeviceResponse updated = deviceRepository.update(id, normalized);
        if (updated == null) {
            throw new DeviceNotFoundException(id);
        }
        return updated;
    }

    @Transactional
    public void deleteDevice(long id) {
        if (!deviceRepository.deleteById(id)) {
            throw new DeviceNotFoundException(id);
        }
    }

    private DeviceRequest validateAndNormalize(DeviceRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("设备请求不能为空");
        }
        if (request.code() == null || request.code().isBlank()) {
            throw new IllegalArgumentException("设备编号不能为空");
        }
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("设备名称不能为空");
        }
        BigDecimal price = request.purchasePrice() == null ? BigDecimal.ZERO : request.purchasePrice();
        if (price.signum() < 0) {
            throw new IllegalArgumentException("设备价格不能为负数");
        }
        String status = request.status() == null || request.status().isBlank() ? "IN_USE" : request.status().trim();
        if (!Set.of("IN_USE", "MAINTENANCE", "RETIRED").contains(status)) {
            throw new IllegalArgumentException("设备状态无效");
        }
        return new DeviceRequest(request.code().trim(), request.name().trim(), request.model(), request.factoryDate(),
                price, request.workshopId(), request.responsiblePerson(), status, request.description());
    }

    private void validateWorkshop(Long workshopId) {
        if (workshopId != null && !deviceRepository.workshopExists(workshopId)) {
            throw new IllegalArgumentException("所属车间不存在");
        }
    }
}
