package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.CyberResourceInfo;
import edu.fudan.se.sctap_lowcode_tool.model.SocialResourceInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.CyberResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CyberResourceService {

    @Autowired
    private CyberResourceRepository cyberResourceRepository;

    public List<CyberResourceInfo> getCyberResourceByProjectId(Integer projectId) {
        return cyberResourceRepository.findByProjectInfoProjectId(projectId);
    }

    public CyberResourceInfo findByResourceId(String resourceId) {
        return cyberResourceRepository.findByResourceId(resourceId);
    }

    /**
     * 获取所有社会服务名称（用于第一个下拉框）
     */
    public List<Map<String, String>> getCyberResource() {
        return cyberResourceRepository.findAll().stream()
                .map(p -> Map.of("value", p.getResourceId(), "label", p.getDescription()))
                .collect(Collectors.toList());
    }

    /**
     * 根据 ResourceId 获取参数 JSON（直接返回 input 字段）
     */
    public String getParamJson(String resourceId) {
        Optional<CyberResourceInfo> socialResourceOpt = Optional.ofNullable(cyberResourceRepository.findByResourceId(resourceId));
        if (socialResourceOpt.isPresent() && socialResourceOpt.get().getCyberResourceJson() != null) {
            return socialResourceOpt.get().getCyberResourceJson();
        }
        return "{}";
    }

    /**
     * 根据 ResourceId 获取服务描述（直接返回 details 字段）
     */
    public String getMoreDetails(String resourceId) {
        Optional<CyberResourceInfo> socialResourceOpt = Optional.ofNullable(cyberResourceRepository.findByResourceId(resourceId));
        if (socialResourceOpt.isPresent() && socialResourceOpt.get().getDetails() != null) {
            return socialResourceOpt.get().getDetails();
        }
        return "{}";
    }

    /**
     * 保存或更新资源信息
     * 对应 Controller 中的 saveCyberResource 调用
     */
    public CyberResourceInfo saveCyberResource(CyberResourceInfo info) {
        info.setLastUpdateTime(java.time.LocalDateTime.now());
        if (info.getState() == null || info.getState().isEmpty()) {
            info.setState("在线");
        }
        if (info.getDetails() == null) {
            info.setDetails(info.getDescription());
        }
        return cyberResourceRepository.save(info);
    }

    /**
     * 根据 ID 删除资源
     * 对应 Controller 中的 deleteCyberResource 调用
     */
    public void deleteCyberResource(Integer id) {
        cyberResourceRepository.deleteById(id);
    }

}
