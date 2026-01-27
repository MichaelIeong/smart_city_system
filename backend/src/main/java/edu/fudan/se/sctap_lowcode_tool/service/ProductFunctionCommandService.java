package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.ProductCommandDTO;
import edu.fudan.se.sctap_lowcode_tool.model.ProductFunctionCommand;
import edu.fudan.se.sctap_lowcode_tool.repository.ProductCommandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductFunctionCommandService {

    @Autowired
    private ProductCommandRepository productCommandRepository;

    public List<ProductCommandDTO> getProductCommands(String productId) {
        // 1. 查询实体列表
        List<ProductFunctionCommand> entities = productCommandRepository.findByProductId(productId);

        // 2. 转换为 DTO 返回 (Entity -> DTO)
        return entities.stream().map(entity -> {
            ProductCommandDTO dto = new ProductCommandDTO();
            dto.setProductId(entity.getProductId());
            dto.setFunctionId(entity.getFunctionId());
            dto.setFunctionName(entity.getFunctionName());
            dto.setCommandId(entity.getCommandId());
            dto.setCommandName(entity.getCommandName());

            // 从关联对象中获取 JSON 数据，注意判空 (LEFT JOIN 可能为空)
            if (entity.getCommandJsonDetail() != null) {
                dto.setCommandJson(entity.getCommandJsonDetail().getCommandJson());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    public List<String> getFunctionsByProductId(String productId){
        return productCommandRepository.getFunctionsByProductId(productId);
    }

    public List<String> getCommandsByFunctionName(String functionName){
        return productCommandRepository.getCommandsNameByFunction(functionName);
    }

    public String getCommandJsonById(String commandId){
        return productCommandRepository.getCommandJsonById(commandId);
    }

}