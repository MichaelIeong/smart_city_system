package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;
import edu.fudan.se.sctap_lowcode_tool.model.SpaceInfo;

import java.util.Optional;
import java.util.Set;

public interface SpaceService {
    SpaceInfo saveOrUpdateSpace(SpaceInfo spaceInfo);

    boolean deleteSpace(String spaceId);

    Optional<SpaceInfo> findSpaceById(String spaceId);

    Set<DeviceInfo> getAllSpaceDevices(String spaceId);

    Iterable<SpaceInfo> findAllSpaces();

    boolean importSpaces(String json);

    Optional<String> exportSpaces();

    boolean addDeviceToSpace(String spaceId, DeviceInfo deviceInfo);

    boolean removeDeviceFromSpace(String spaceId, String deviceId);
}