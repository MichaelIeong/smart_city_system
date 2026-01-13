package edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.common_operator;

/**
 * CommonOperatorPreviewResponse 通用算子预览响应体
 * <p>
 * 只包含通用算子的名称和描述信息，用于在前端展示通用算子列表时使用。
 * </p>
 * @param operatorName 算子名称
 * @param description 算子描述信息
 */
public record CommonOperatorPreviewResponse(
    String operatorName,
    String description
) {}
