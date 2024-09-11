package edu.fudan.se.sctap_lowcode_tool.service.impl;

import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;
import edu.fudan.se.sctap_lowcode_tool.model.DeviceTypeInfo;
import edu.fudan.se.sctap_lowcode_tool.model.EventInfo;
import edu.fudan.se.sctap_lowcode_tool.model.SpaceInfo;
import edu.fudan.se.sctap_lowcode_tool.model.import_json.meta.CustomerThing;
import edu.fudan.se.sctap_lowcode_tool.model.import_json.meta.Meta;
import edu.fudan.se.sctap_lowcode_tool.repository.DeviceRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.DeviceTypeRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.EventRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.SpaceRepository;
import edu.fudan.se.sctap_lowcode_tool.service.ImportService;
import edu.fudan.se.sctap_lowcode_tool.utils.import_utils.InvalidJsonValueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Service
public class ImportServiceImpl implements ImportService {

    private static final Logger log = LoggerFactory.getLogger(ImportServiceImpl.class);

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceTypeRepository deviceTypeRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private EventRepository eventRepository;

    @Override
    public boolean isSensor(Meta meta) {
        return meta.DeviceType().contains("传感器");
    }

    @Override
    public SpaceInfo toSpaceInfo(Meta meta) {
        SpaceInfo spaceInfo = new SpaceInfo();
        spaceInfo.setSpaceId(meta.Id());
        spaceInfo.setSpaceName(meta.Name());
        return spaceInfo;
    }
    
    @Override
    public EventInfo toEventInfo(Meta spaceMeta, CustomerThing event) throws InvalidJsonValueException {
        requireNonNull(spaceMeta, "event.Id", event.Id());
        requireNonNull(spaceMeta, "event.Name", event.Name());
        EventInfo eventInfo = new EventInfo();
        eventInfo.setEventId(event.Id());
        eventInfo.setEventType(event.Name());
        eventInfo.setLocation(spaceMeta.Name());
        eventInfo.setObjectId(spaceMeta.Id());
        return eventInfo;
    }

    @Override
    public DeviceTypeInfo toDeviceTypeInfo(Meta meta) throws InvalidJsonValueException {
        requireNonNull(meta, "meta.DeviceType", meta.DeviceType());
        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        deviceTypeInfo.setIsSensor(this.isSensor(meta));
        deviceTypeInfo.setDeviceTypeName(meta.DeviceType());
        return deviceTypeInfo;
    }

    @Override
    public DeviceInfo toDeviceInfo(Meta meta) throws InvalidJsonValueException {
        requireNonNull(meta, "meta.DeviceId", meta.DeviceId());
        requireNonNull(meta, "meta.DeviceName", meta.DeviceName());
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setCoordinateX(meta.GraphicPosition().X());
        deviceInfo.setCoordinateY(meta.GraphicPosition().Y());
        deviceInfo.setCoordinateZ(meta.GraphicPosition().Z());
        deviceInfo.setIsSensor(this.isSensor(meta));
        deviceInfo.setDeviceId(meta.DeviceId());
        deviceInfo.setDeviceName(meta.DeviceName());
        return deviceInfo;
    }

    private void requireNonNull(Meta meta, String key, Object value) throws InvalidJsonValueException {
        if (value == null)
            throw new InvalidJsonValueException(meta.Name(), "value cannot be null", key, "null");
    }

    /**
     * 向数据库中添加设备信息及设备类型信息 <br>
     * ! 若该Meta信息非设备, 跳过 <br>
     * ! 跳过数据库中已存在的设备 <br>
     * ! 若设备信息中的值不合法，跳过该设备的导入，不报错 <br>
     * @param deviceMeta 该设备的Meta信息
     * @param spaceMeta 该设备所在空间的Meta信息
     */
    @Override
    public void addDevice(Meta deviceMeta, Meta spaceMeta) {
        try {
            if (deviceMeta == null || !deviceMeta.IsDevice()) return;
            // Add Device Type
            if (!deviceTypeRepository.existsByDeviceTypeName(deviceMeta.DeviceType())){
                DeviceTypeInfo deviceTypeInfo = this.toDeviceTypeInfo(deviceMeta);
                deviceTypeRepository.save(deviceTypeInfo);
            }
            // Add Device
            if (deviceRepository.existsById(deviceMeta.DeviceId())) return;
            DeviceInfo deviceInfo = this.toDeviceInfo(deviceMeta);
            deviceRepository.save(deviceInfo);
            // Add Device Reference to Space
            SpaceInfo spaceInfo = spaceRepository.findById(spaceMeta.Id()).orElseGet(() -> {
                this.addSpace(spaceMeta);
                return spaceRepository.getReferenceById(spaceMeta.Id());
            });
            Set<DeviceInfo> devices = spaceInfo.getSpaceDevices();
            if (devices == null) {
                devices = new HashSet<>();
            }
            devices.add(deviceInfo);
            spaceInfo.setSpaceDevices(devices);
            spaceRepository.save(spaceInfo);
        } catch (InvalidJsonValueException e) {
            log.warn(e.getMessage());
        }
    }

    /**
     * 向数据库中添加空间和事件信息 <br>
     * ! 跳过数据库中已存在的空间 <br>
     * ! 若事件信息中的值不合法，跳过该事件的导入，不报错 <br>
     * @param meta 该空间的Meta信息
     */
    @Override
    public void addSpace(Meta meta) {
        if (meta == null) return;
        // Add Space
        SpaceInfo spaceInfo = this.toSpaceInfo(meta);
        if (spaceRepository.existsById(spaceInfo.getSpaceId())) return;
        spaceRepository.save(spaceInfo);
        // Add Events
        if (meta.CustomerEvents() == null) return;
        meta.CustomerEvents().forEach(event -> {
            try {
                EventInfo eventInfo = this.toEventInfo(meta, event);
                eventRepository.save(eventInfo);
            } catch (InvalidJsonValueException e) {
                log.warn(e.getMessage());
            }
        });
    }

    @Transactional
    @Override
    public void importMetaRecursively(Iterator<Meta> metaIterator) {
        metaIterator.forEachRemaining(meta -> {
            this.addSpace(meta);
            meta.getChildrenDevices().forEach(childDevice ->
                    this.addDevice(childDevice, meta)
            );
        });

    }

}
