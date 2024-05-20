package edu.fudan.se.sctap_lowcode_tool.service.impl;

import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;
import edu.fudan.se.sctap_lowcode_tool.model.SpaceInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.SpaceRepository;
import edu.fudan.se.sctap_lowcode_tool.service.SpaceService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class SpaceServiceImpl implements SpaceService {

    @Autowired
    private SpaceRepository spaceRepository;

    @Override
    public SpaceInfo saveOrUpdateSpace(SpaceInfo spaceInfo) {
        return spaceRepository.save(spaceInfo);
    }

    @Override
    public boolean deleteSpace(int spaceId) {
        if (spaceRepository.existsById(spaceId)) {
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
    public int getSpaceIdByName(String spaceName) {
        return spaceRepository.findIdBySpaceName(spaceName)
                .orElseThrow(() -> new EntityNotFoundException("Space not found with name: " + spaceName));
    }


    @Override
    public Iterable<SpaceInfo> findAllSpaces() {
        return spaceRepository.findAll();
    }
}