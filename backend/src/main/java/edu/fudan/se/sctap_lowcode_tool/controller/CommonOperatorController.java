package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.common_operator.CommonOperatorDetailResponse;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.common_operator.CommonOperatorPreviewResponse;
import edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.common_operator.CommonOperatorRegistry;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/common-operators")
@Tag(name = "CommonOperatorController", description = "通用算子信息获取接口")
@RequiredArgsConstructor
class CommonOperatorController {

    private final CommonOperatorRegistry commonOperatorRegistry;

    @GetMapping
    @Tag(name = "获取所有通用算子")
    public List<CommonOperatorPreviewResponse> getAllCommonOperators() {
        return commonOperatorRegistry.listAllOperators();
    }

    @GetMapping("/{name}")
    @Tag(name = "获取指定通用算子详情")
    public CommonOperatorDetailResponse getCommonOperatorById(@PathVariable String name) {
        return commonOperatorRegistry.getOperatorInfo(name);
    }
}
