package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;
import edu.fudan.se.sctap_lowcode_tool.model.SpaceInfo;

import java.util.Optional;
import java.util.Set;

public interface SpaceService {
    SpaceInfo saveOrUpdateSpace(SpaceInfo spaceInfo);

    boolean deleteSpace(int spaceId);

    Optional<SpaceInfo> findSpaceById(int spaceId);

    Set<DeviceInfo> getAllSpaceDevices(int spaceId);

    Iterable<SpaceInfo> findAllSpaces();

    boolean importSpaces(String json);

    Optional<String> exportSpaces();

    boolean addDeviceToSpace(int spaceId, DeviceInfo deviceInfo);

    boolean removeDeviceFromSpace(int spaceId, int deviceId);
}