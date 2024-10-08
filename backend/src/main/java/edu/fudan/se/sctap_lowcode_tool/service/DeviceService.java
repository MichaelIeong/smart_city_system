package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;

import java.util.Optional;

public interface DeviceService {
    DeviceInfo saveOrUpdateDevice(DeviceInfo deviceInfo); // 保存或更新设备信息

    boolean deleteDevice(int deviceID); // 删除设备

    Optional<DeviceInfo> findById(int deviceID); // 根据ID查找设备

    Iterable<DeviceInfo> findAll(); // 获取所有设备

    boolean importDevices(String json);

    Optional<String> exportDevices();
}
