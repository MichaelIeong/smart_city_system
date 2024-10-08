package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.DeviceTypeInfo;

import java.util.Optional;

public interface DeviceTypeService {
    DeviceTypeInfo saveOrUpdateDeviceType(DeviceTypeInfo deviceTypeInfo); // 保存或更新设备类型信息

    boolean deleteType(int deviceID); // 删除设备类型

    Optional<DeviceTypeInfo> findById(int deviceID); // 根据ID查找设备类型

    Boolean getTypeIsSensor(int deviceID); // 获取设备类型是否为传感器

    Iterable<DeviceTypeInfo> findAll(); // 获取所有设备类型
}
