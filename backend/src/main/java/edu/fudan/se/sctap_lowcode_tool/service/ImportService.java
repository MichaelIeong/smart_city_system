package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;
import edu.fudan.se.sctap_lowcode_tool.model.DeviceTypeInfo;
import edu.fudan.se.sctap_lowcode_tool.model.EventInfo;
import edu.fudan.se.sctap_lowcode_tool.model.SpaceInfo;
import edu.fudan.se.sctap_lowcode_tool.model.import_json.meta.CustomerThing;
import edu.fudan.se.sctap_lowcode_tool.model.import_json.meta.Meta;
import edu.fudan.se.sctap_lowcode_tool.utils.import_utils.InvalidJsonValueException;

import java.util.Iterator;

public interface ImportService {

    boolean isSensor(Meta meta);

    SpaceInfo toSpaceInfo(Meta meta);

    EventInfo toEventInfo(Meta meta, CustomerThing event) throws InvalidJsonValueException;

    DeviceInfo toDeviceInfo(Meta meta) throws InvalidJsonValueException;

    void addSpace(Meta meta);

    void addDevice(Meta deviceMeta, Meta spaceMeta);

    DeviceTypeInfo toDeviceTypeInfo(Meta meta) throws InvalidJsonValueException;

    void importMetaRecursively(Iterator<Meta> metaIterator);

}
