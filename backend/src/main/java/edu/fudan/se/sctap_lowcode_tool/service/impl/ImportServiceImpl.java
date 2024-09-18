package edu.fudan.se.sctap_lowcode_tool.service.impl;

import edu.fudan.se.sctap_lowcode_tool.model.*;
import edu.fudan.se.sctap_lowcode_tool.model.import_json.meta.CustomerThing;
import edu.fudan.se.sctap_lowcode_tool.model.import_json.meta.Meta;
import edu.fudan.se.sctap_lowcode_tool.repository.*;
import edu.fudan.se.sctap_lowcode_tool.service.ImportService;
import edu.fudan.se.sctap_lowcode_tool.utils.import_utils.InvalidJsonValueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;

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

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private PropertySpaceRepository propertySpaceRepository;

    /**
     * Add DeviceInfo and DeviceTypeInfo to the database. <br>
     *
     * details: <br>
     * - Add Device,
     * - Add DeviceType,
     * - Add Device-Space Reference <br>
     *
     * warnings: <br>
     * ! Skip if the given Meta object is not a device, but a space or wall instead. <br>
     * ! Skip if the device already exists in the database. <br>
     * ! Skip if the device information is invalid, log this, but do not throw an error. <br>
     * @param deviceMeta the Meta object representing the device
     * @param spaceMeta the Meta object representing the space where the device is located
     */
    public void addDevice(Meta deviceMeta, Meta spaceMeta) {
        try {
            if (deviceMeta == null || !deviceMeta.IsDevice()) return;
            // Add Device Type
            if (!deviceTypeRepository.existsByDeviceTypeName(deviceMeta.DeviceType())){
                DeviceTypeInfo deviceTypeInfo = this.toDeviceTypeInfo(deviceMeta);
                deviceTypeRepository.save(deviceTypeInfo);
            }
            // Add Device
//            if (deviceRepository.existsById(deviceMeta.DeviceId())) return;
            DeviceInfo deviceInfo = this.toDeviceInfo(deviceMeta);
            deviceRepository.save(deviceInfo);
            // Add Device Reference to Space
//            SpaceInfo spaceInfo = spaceRepository.findById(spaceMeta.Id()).orElseThrow();
//            spaceInfo.getSpaceDevices().add(deviceInfo);
//            spaceRepository.save(spaceInfo);
        } catch (InvalidJsonValueException e) {
            log.warn(e.getMessage());
        }
    }

    ;

    /**
     * Checks if the given Meta object represents a sensor.
     *
     * @param meta the Meta object to check
     * @return true if the Meta object represents a sensor, false otherwise
     */
    public boolean isSensor(Meta meta) {
        return meta.DeviceType().contains("传感器");
    }

    /**
     * Converts a Meta object to a SpaceInfo object.
     * details: <br>
     * - SpaceId: meta.Id <br>
     * - SpaceName: meta.Name <br>
     *
     * @param meta the Meta object to convert
     * @return the converted SpaceInfo object
     */
    public SpaceInfo toSpaceInfo(Meta meta) {
        SpaceInfo spaceInfo = new SpaceInfo();
        spaceInfo.setSpaceId(meta.Id());
        spaceInfo.setSpaceName(meta.Name());
        return spaceInfo;
    }

    /**
     * Converts an event object and its parenting meta to an EventInfo object.
     * details: <br>
     * - EventId: event.Id (requireNonNull) <br>
     * - EventType: event.Name (requireNonNull) <br>
     *
     * @param spaceMeta the Meta object of the space where the event occurs
     * @param event     the CustomerThing object representing the event
     * @return the converted EventInfo object
     * @throws InvalidJsonValueException if violates the non-null constraint
     */
    public EventInfo toEventInfo(Meta spaceMeta, CustomerThing event) throws InvalidJsonValueException {
        requireNonNull(spaceMeta, "event.Id", event.Id());
        requireNonNull(spaceMeta, "event.Name", event.Name());
        EventInfo eventInfo = new EventInfo();
        eventInfo.setEventId(event.Id());
        eventInfo.setEventType(event.Name());
        return eventInfo;
    }


    /**
     * Converts a Meta object to a DeviceTypeInfo object.
     * details: <br>
     * - DeviceTypeName: meta.DeviceType (requireNonNull) <br>
     * - IsSensor: using method isSensor(meta) <br>
     *
     * @param meta the Meta object to convert
     * @return the converted DeviceTypeInfo object
     * @throws InvalidJsonValueException if violates the non-null constraint
     */
    public DeviceTypeInfo toDeviceTypeInfo(Meta meta) throws InvalidJsonValueException {
        requireNonNull(meta, "meta.DeviceType", meta.DeviceType());
        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        deviceTypeInfo.setIsSensor(this.isSensor(meta));
        deviceTypeInfo.setDeviceTypeName(meta.DeviceType());
        return deviceTypeInfo;
    }

    /**
     * Converts a Meta object to a DeviceInfo object.
     * details: <br>
     * - CoordinateX/Y/Z: meta.GraphicPosition.X/Y/Z <br>
     * - IsSensor: using method isSensor(meta) <br>
     * - DeviceId: meta.DeviceId (requireNonNull) <br>
     * - DeviceName: meta.DeviceName (requireNonNull) <br>
     *
     * @param meta the Meta object to convert
     * @return the converted DeviceInfo object
     * @throws InvalidJsonValueException if violates the non-null constraint
     */
    public DeviceInfo toDeviceInfo(Meta meta) throws InvalidJsonValueException {
        requireNonNull(meta, "meta.DeviceId", meta.DeviceId());
        requireNonNull(meta, "meta.DeviceName", meta.DeviceName());
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setCoordinateX(meta.GraphicPosition().X());
        deviceInfo.setCoordinateY(meta.GraphicPosition().Y());
        deviceInfo.setCoordinateZ(meta.GraphicPosition().Z());
        deviceInfo.setDeviceId(meta.DeviceId());
        deviceInfo.setDeviceName(meta.DeviceName());
        return deviceInfo;
    }

    private void requireNonNull(Meta meta, String key, Object value) throws InvalidJsonValueException {
        if (value == null)
            throw new InvalidJsonValueException(meta.Name(), "value cannot be null", key, "null");
    }

    /**
     * Add SpaceInfo and EventInfo to the database. <br>
     * <p>
     * details:
     * - Add Space,
     * - Add the relation between this Space and the Project,
     * - Add its Events,
     * - Add its Properties <br>
     * <p>
     * warnings: <br>
     * ! Skip if the space already exists in the database. <br>
     * ! Skip if the event information is invalid, log this, but do not throw an error. <br>
     *
     * @param meta        the Meta object representing the space
     * @param projectInfo the ProjectInfo object where the space is located
     */
    public void addSpace(Meta meta, ProjectInfo projectInfo) {
        if (meta == null) return;
        // Add Space
//        SpaceInfo spaceInfo = this.toSpaceInfo(meta, );
//        if (spaceRepository.existsById(spaceInfo.getSpaceId())) return;
//        spaceRepository.save(spaceInfo);
        // Add Space-Project Reference


        // Add Properties

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

    // TODO: maintain the relations of adjacent spaces

    record PropertyPair(PropertyInfo propertyInfo, PropertySpace propertySpace) {
    }

    /**
     * **Entry Method of this class** <br>
     * Import the Meta objects recursively. <br>
     * <p>
     * - Make a new ProjectInfo and
     * - for each remaining space in the iterator, <br>
     * - Add SpaceInfo and EventInfo (using method addSpaceAndEvent) <br>
     * - Add All Devices and DeviceTypes in the space (using method addDevice) <br>
     * <p>
     * ! Skip all existing and invalid information, log this, but do not throw an error. <br>
     *
     * @param metaIterator the iterator of Meta objects to import
     * @param projectName the project name to be saved
     */
    @Transactional
    @Override
    public void importMetaRecursively(Iterator<Meta> metaIterator, String projectName) {

        // Make a new ProjectInfo and save
        ProjectInfo projectInfo = new ProjectInfo();
        projectInfo.setProjectName(projectName);
        projectRepository.save(projectInfo);

        metaIterator.forEachRemaining(meta -> {
            this.addSpace(meta, projectInfo);
            meta.getChildrenDevices().forEach(childDevice ->
                    this.addDevice(childDevice, meta)
            );
        });

    }

}
