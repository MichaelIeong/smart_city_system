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
    public boolean deleteSpace(int spaceId) {
        if (spaceRepository.existsById(spaceId)) {
            // 删除与该空间关联的所有设备
            List<DeviceInfo> devices = deviceRepository.findBySpaceId(spaceId);
            deviceRepository.deleteAll(devices);

            // 删除空间
            spaceRepository.deleteById(spaceId);
            return true;
        }
        return false;
    }

    @Override
    public Optional<SpaceInfo> findSpaceById(int spaceId) {
        return spaceRepository.findById(spaceId);
    }

    @Override
    public Set<DeviceInfo> getAllSpaceDevices(int spaceId) {
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
}