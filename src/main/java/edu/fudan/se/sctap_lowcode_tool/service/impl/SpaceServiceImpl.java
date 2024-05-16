package edu.fudan.se.sctap_lowcode_tool.service.impl;

import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;
import edu.fudan.se.sctap_lowcode_tool.model.SpaceInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.SpaceRepository;
import edu.fudan.se.sctap_lowcode_tool.service.SpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;


@Service
public class SpaceServiceImpl implements SpaceService {

    @Autowired
    private SpaceRepository spaceRepository;

    @Override
    public SpaceInfo saveSpaceInfo(SpaceInfo spaceInfo) {
        return spaceRepository.save(spaceInfo);
    }

    @Override
    public SpaceInfo getSpaceInfoById(int spaceId) {
        return spaceRepository.findById(spaceId).orElse(null);
    }

    @Override
    public SpaceInfo updateSpaceInfo(SpaceInfo spaceInfo) {
        return spaceRepository.save(spaceInfo);
    }

    @Override
    public void deleteSpaceInfoById(int spaceId) {
        spaceRepository.deleteById(spaceId);
    }

//    @Override
//    public Set<DeviceInfo> getAllSpaceDevices(int spaceId) {
//        SpaceInfo spaceInfo = spaceRepository.findById(spaceId).orElse(null);
//        if (spaceInfo != null) {
//            return spaceInfo.getDevices();
//        }
//        return null;
//    }
}