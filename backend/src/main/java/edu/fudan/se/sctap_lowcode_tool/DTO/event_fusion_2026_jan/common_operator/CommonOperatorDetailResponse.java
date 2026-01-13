package edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.common_operator;

import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.Var;

import java.util.List;

/**
 * CommonOperatorDetailResponse 通用算子详情响应体
 * <p>
 * 包含内置通用算子的名称、描述信息、入参格式和出参格式等详细信息。
 * </p>
 * @param operatorName 算子名称
 * @param description 算子描述信息
 * @param inputSpec 算子输入参数规范
 * @param outputSpec 算子输出参数规范
 */
public record CommonOperatorDetailResponse(
    String operatorName,
    String description,
    List<Var> inputSpec,
    List<Var> outputSpec
) {}
