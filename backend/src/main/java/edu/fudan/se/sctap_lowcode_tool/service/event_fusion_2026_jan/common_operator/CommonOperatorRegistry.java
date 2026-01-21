package edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.common_operator;

import edu.fudan.se.sctap_lowcode_tool.DTO.BadRequestException;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.Var;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.common_operator.CommonOperatorDetailResponse;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.common_operator.CommonOperatorPreviewResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CommonOperatorRegistry 通用算子注册中心
 * <p>
 * 维护算子名称到算子实例的映射关系，支持算子的注册、查询和获取。
 * 在 Spring 容器启动时自动扫描所有 CommonOperator 实现类并注册。
 * </p>
 */
@Slf4j
@Component
public class CommonOperatorRegistry {

    private final Map<String, CommonOperator> operatorMap = new HashMap<>();

    /**
     * 在 SpringBoot 应用启动时，自动扫描并注册所有 CommonOperator 实现类
     *
     * @param operators Spring 自动注入的所有 CommonOperator Bean
     */
    public CommonOperatorRegistry(List<CommonOperator> operators) {
        operators.forEach(operator -> {
            String operatorName = operator.getClass().getSimpleName();
            operatorMap.put(operatorName, operator);
        });
    }

    /**
     * 根据算子名称获取算子实例
     *
     * @param operatorName 算子名称（类名，如 "Count"、"TextCompare"）
     * @return 算子实例
     * @throws BadRequestException 当算子不存在时
     */
    public CommonOperator getOperator(String operatorName) throws BadRequestException {
        CommonOperator operator = operatorMap.get(operatorName);
        if (operator == null) {
            throw new BadRequestException(
                "400",
                "要使用的通用算子: " + operatorName + " 不存在",
                "operatorName",
                operatorName,
                "未找到名为 " + operatorName + " 的通用算子"
            );
        }
        return operator;
    }

    /**
     * 获取指定算子的详细信息
     *
     * @param operatorName 算子名称
     * @return 算子详细信息响应对象（包括名称、描述、输入输出参数规范）
     * @throws BadRequestException 当算子不存在时
     */
    public CommonOperatorDetailResponse getOperatorInfo(String operatorName) throws BadRequestException {
        CommonOperator operator = getOperator(operatorName);
        String description = operator.getDescription();
        List<Var> inputSpec = operator.getInputSpec();
        List<Var> outputSpec = operator.getOutputSpec();
        return new CommonOperatorDetailResponse(operatorName, description, inputSpec, outputSpec);
    }

    /**
     * 列出所有已注册的算子预览信息
     *
     * @return 算子预览信息列表（包括名称和描述）
     */
    public List<CommonOperatorPreviewResponse> listAllOperators() {
        return operatorMap.entrySet().stream()
            .map(entry -> {
                String name = entry.getKey();
                String desc = entry.getValue().getDescription();
                return new CommonOperatorPreviewResponse(name, desc);
            })
            .toList();
    }

}
