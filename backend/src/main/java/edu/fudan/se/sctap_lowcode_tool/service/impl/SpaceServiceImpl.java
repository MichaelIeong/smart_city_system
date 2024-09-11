package edu.fudan.se.sctap_lowcode_tool.service.impl;

import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;
import edu.fudan.se.sctap_lowcode_tool.model.SpaceInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.DeviceRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.SpaceRepository;
import edu.fudan.se.sctap_lowcode_tool.service.SpaceService;
import edu.fudan.se.sctap_lowcode_tool.utils.JsonUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class SpaceServiceImpl implements SpaceService {

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private JsonUtil jsonUtil;

    @Override
    public SpaceInfo saveOrUpdateSpace(SpaceInfo spaceInfo) {
        return spaceRepository.save(spaceInfo);
    }

    @Override
    public boolean deleteSpace(String spaceId) {
        if (spaceRepository.existsById(spaceId)) {
            spaceRepository.deleteById(spaceId);
            return true;
        }
        return false;
    }

    @Override
    public Optional<SpaceInfo> findSpaceById(String spaceId) {
        return spaceRepository.findById(spaceId);
    }

    @Override
    public Set<DeviceInfo> getAllSpaceDevices(String spaceId) {
        return spaceRepository.findById(spaceId)
                .map(SpaceInfo::getSpaceDevices)
                .orElseThrow(() -> new EntityNotFoundException("No any devices in space with id: " + spaceId));
    }

    @Override
    public Iterable<SpaceInfo> findAllSpaces() {
        return spaceRepository.findAll();
    }

    @Override
    public boolean importSpaces(String json) {
        return Optional.ofNullable(json)
                .map(j -> jsonUtil.parseJsonToList(j, SpaceInfo.class))
                .map(spaces -> {
                    spaces.forEach(this::saveOrUpdateSpace);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public Optional<String> exportSpaces() {
        return Optional.ofNullable(findAllSpaces())
                .map(spaces -> jsonUtil.convertListToJson((List<SpaceInfo>) spaces));
    }

    @Override
    public boolean addDeviceToSpace(String spaceId, DeviceInfo deviceInfo) {
        Optional<SpaceInfo> spaceOpt = spaceRepository.findById(spaceId);
        if (spaceOpt.isPresent()) {
            SpaceInfo space = spaceOpt.get();
            space.getSpaceDevices().add(deviceInfo);
            deviceRepository.save(deviceInfo);
            spaceRepository.save(space);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeDeviceFromSpace(String spaceId, String deviceId) {
        Optional<SpaceInfo> spaceOpt = spaceRepository.findById(spaceId);
        if (spaceOpt.isPresent()) {
            SpaceInfo space = spaceOpt.get();
            Optional<DeviceInfo> deviceOpt = deviceRepository.findById(deviceId);
            if (deviceOpt.isPresent()) {
                space.getSpaceDevices().remove(deviceOpt.get());
                spaceRepository.save(space);
                return true;
            }
        }
        return false;
    }
}