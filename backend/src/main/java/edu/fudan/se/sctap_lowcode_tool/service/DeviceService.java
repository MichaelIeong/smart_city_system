package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;

import java.util.Optional;

public interface DeviceService {
    DeviceInfo saveOrUpdateDevice(DeviceInfo deviceInfo); // 保存或更新设备信息

    boolean deleteDevice(int deviceID); // 删除设备

    Optional<DeviceInfo> findById(int deviceID); // 根据ID查找设备

    String getDeviceStatus(int deviceID); // 获取设备状态

    String getDeviceURL(int deviceID); // 获取设备的URL

    String getDeviceData(int deviceID); // 获取设备的数据

    String getDeviceType(int deviceID); // 获取设备的类型

    Boolean getDeviceIsSensor(int deviceID); // 获取设备是否为传感器

    String getDeviceCapabilities(int deviceID); // 获取设备的能力

    Iterable<DeviceInfo> findAll(); // 获取所有设备

    boolean importDevices(String json);

    Optional<String> exportDevices();
}
