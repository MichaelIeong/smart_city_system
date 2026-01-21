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
     * 根据 ResourceType 获取参数 JSON（
     */
    public String getParamJson(String resourceType) {
        /*
        Optional<CyberResourceInfo> socialResourceOpt = Optional.ofNullable(cyberResourceRepository.findByResourceId(resourceId));
        if (socialResourceOpt.isPresent() && socialResourceOpt.get().getCyberResourceJson() != null) {
            return socialResourceOpt.get().getCyberResourceJson();
        }
        return "{}";
         */
        List<CyberResourceInfo> resources = cyberResourceRepository.findByResourceType(resourceType);

        if (resources != null && !resources.isEmpty()) {
            // 取第一个
            CyberResourceInfo first = resources.get(0);
            if (first.getCyberResourceJson() != null) {
                return first.getCyberResourceJson();
            }
        }

        return "{}";
    }

    /**
     * 根据 ResourceType 获取服务描述（直接返回 details 字段）
     */
    public String getMoreDetails(String resourceType) {
        /*
        Optional<CyberResourceInfo> socialResourceOpt = Optional.ofNullable(cyberResourceRepository.findByResourceId(resourceId));
        if (socialResourceOpt.isPresent() && socialResourceOpt.get().getDetails() != null) {
            return socialResourceOpt.get().getDetails();
        }
        return "{}";
        */
        List<CyberResourceInfo> resources = cyberResourceRepository.findByResourceType(resourceType);

        if (resources != null && !resources.isEmpty()) {
            // 取第一个
            CyberResourceInfo first = resources.get(0);
            if (first.getDetails() != null) {
                return first.getDetails();
            }
        }

        return "{}";
    }

    public List<Map<String, String>> getCyberResourceTypes() {
        return cyberResourceRepository.findAll().stream()
                .collect(Collectors.toMap(
                        p -> p.getResourceType(), // 以 resource_type (description) 为 key
                        p -> Map.of("value", p.getResourceId(), "label", p.getResourceType()),
                        (existing, replacement) -> existing // 如果 key 冲突，保留第一个（或替换逻辑）
                ))
                .values()
                .stream()
                .collect(Collectors.toList());
    }
}
