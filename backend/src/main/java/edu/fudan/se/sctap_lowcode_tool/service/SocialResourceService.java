package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.Product;
import edu.fudan.se.sctap_lowcode_tool.model.SocialResourceInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.SocialResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional("jpaTransactionManager")
public class SocialResourceService {

    @Autowired
    private SocialResourceRepository socialResourceRepository;

    public List<SocialResourceInfo> getSocialResourceByProjectId(Integer projectId) {
        return socialResourceRepository.findByProjectInfoProjectId(projectId);
    }

    public SocialResourceInfo findByResourceId(String resourceId) {
        return socialResourceRepository.findByResourceId(resourceId);
    }

    /**
     * 获取所有社会服务名称（用于第一个下拉框）
     */
    public List<Map<String, String>> getSocialResource() {
        return socialResourceRepository.findAll().stream()
                .map(p -> Map.of("value", p.getResourceId(), "label", p.getResourceType()))
                .collect(Collectors.toList());
    }

    /**
     * 根据 ResourceType 获取参数 JSON（直接返回 input 字段）
     */
    public String getParamJson(String resourceType) {
        /*
        Optional<SocialResourceInfo> socialResourceOpt = Optional.ofNullable(socialResourceRepository.findByResourceType(resourceType));
        if (socialResourceOpt.isPresent() && socialResourceOpt.get().getSocialResourceJson() != null) {
            return socialResourceOpt.get().getSocialResourceJson();
        }
        return "{}";
        */
        List<SocialResourceInfo> resources = socialResourceRepository.findByResourceType(resourceType);

        if (resources != null && !resources.isEmpty()) {
            // 取第一个
            SocialResourceInfo first = resources.get(0);
            if (first.getSocialResourceJson() != null) {
                return first.getSocialResourceJson();
            }
        }

        return "{}";
    }

    /**
     * 根据 ResourceType获取服务描述（直接返回 details 字段）
     */
    public String getMoreDetails(String resourceType) {
        /**
        Optional<SocialResourceInfo> socialResourceOpt = Optional.ofNullable(socialResourceRepository.findByResourceType(resourceType));
        if (socialResourceOpt.isPresent() && socialResourceOpt.get().getDetails() != null) {
            return socialResourceOpt.get().getDetails();
        }
        return "{}";*/
        List<SocialResourceInfo> resources = socialResourceRepository.findByResourceType(resourceType);

        if (resources != null && !resources.isEmpty()) {
            // 取第一个
            SocialResourceInfo first = resources.get(0);
            if (first.getDetails() != null) {
                return first.getDetails();
            }
        }

        return "{}";
    }

    public List<Map<String, String>> getSocialResourceTypes() {
        return socialResourceRepository.findAll().stream()
                .collect(Collectors.toMap(
                        p -> p.getDescription(), // 以 resource_type (description) 为 key
                        p -> Map.of("value", p.getResourceId(), "label", p.getResourceType()),
                        (existing, replacement) -> existing // 如果 key 冲突，保留第一个（或替换逻辑）
                ))
                .values()
                .stream()
                .collect(Collectors.toList());
    }

}
