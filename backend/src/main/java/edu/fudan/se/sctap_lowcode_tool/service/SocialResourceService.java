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
                .map(p -> Map.of("value", p.getResourceId(), "label", p.getDescription()))
                .collect(Collectors.toList());
    }

    /**
     * 根据 ResourceId 获取参数 JSON（直接返回 input 字段）
     */
    public String getParamJson(String resourceId) {
        Optional<SocialResourceInfo> socialResourceOpt = Optional.ofNullable(socialResourceRepository.findByResourceId(resourceId));
        if (socialResourceOpt.isPresent() && socialResourceOpt.get().getSocialResourceJson() != null) {
            return socialResourceOpt.get().getSocialResourceJson();
        }
        return "{}";
    }

    /**
     * 根据 ResourceId 获取服务描述（直接返回 details 字段）
     */
    public String getMoreDetails(String resourceId) {
        Optional<SocialResourceInfo> socialResourceOpt = Optional.ofNullable(socialResourceRepository.findByResourceId(resourceId));
        if (socialResourceOpt.isPresent() && socialResourceOpt.get().getDetails() != null) {
            return socialResourceOpt.get().getDetails();
        }
        return "{}";
    }

}
