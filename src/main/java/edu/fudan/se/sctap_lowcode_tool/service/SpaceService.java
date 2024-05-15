package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;
import edu.fudan.se.sctap_lowcode_tool.model.SpaceInfo;

import java.util.Set;

public interface SpaceService {
    SpaceInfo saveSpaceInfo(SpaceInfo spaceInfo);

    SpaceInfo getSpaceInfoById(int spaceId);

    SpaceInfo updateSpaceInfo(SpaceInfo spaceInfo);

    void deleteSpaceInfoById(int spaceId);

//    Set<DeviceInfo> getAllSpaceDevices(int spaceId);
}

