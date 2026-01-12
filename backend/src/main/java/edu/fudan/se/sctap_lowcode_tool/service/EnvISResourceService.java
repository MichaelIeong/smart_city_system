package edu.fudan.se.sctap_lowcode_tool.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.fudan.se.sctap_lowcode_tool.repository.EnvInformationResourceRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.EnvSocialResourceRepository;
import edu.fudan.se.sctap_lowcode_tool.model.vo.EnvISResourceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnvISResourceService {

    @Autowired
    private EnvInformationResourceRepository infoRepo;

    @Autowired
    private EnvSocialResourceRepository socialRepo;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取所有信息资源
     */
    public List<EnvISResourceVO> findAllInformation() {
        return infoRepo.findAll().stream()
                .map(entity -> convertToVO(entity.getDescription(), entity.getUrl(), entity.getInput(), entity.getOutput()))
                .collect(Collectors.toList());
    }

    /**
     * 获取所有社会资源
     */
    public List<EnvISResourceVO> findAllSocial() {
        return socialRepo.findAll().stream()
                .map(entity -> convertToVO(entity.getDescription(), entity.getUrl(), entity.getInput(), entity.getOutput()))
                .collect(Collectors.toList());
    }

    /**
     * 统一转换逻辑
     * @param description 对应数据库 description 字段
     * @param url 对应数据库 url 字段
     */
    private EnvISResourceVO convertToVO(String description, String url, String input, String output) {
        EnvISResourceVO vo = new EnvISResourceVO();

        // 1. 映射名称：将数据库的 description 映射为前端要求的 resourceName
        vo.setResourceName(description);

        // 2. 映射URL：将数据库的 url 映射为前端要求的 resourceUrl
        vo.setResourceUrl(url);

        try {
            // 3. 处理 JSON：将 String 类型的 input/output 转换为 JSON 对象/数组
            if (input != null && !input.isEmpty()) {
                vo.setInput(objectMapper.readTree(input));
            }
            if (output != null && !output.isEmpty()) {
                vo.setOutput(objectMapper.readTree(output));
            }
        } catch (Exception e) {
            // 容错处理：转换失败则保留原始字符串
            vo.setInput(input);
            vo.setOutput(output);
        }
        return vo;
    }
}