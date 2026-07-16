package com.jia.equipmentledger.device;

public class DeviceNotFoundException extends RuntimeException {

    public DeviceNotFoundException(long id) {
        super("设备不存在: " + id);
    }
}
