package edu.fudan.se.sctap_lowcode_tool.neo4jModel;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.*;

@Node("Service")
@Data
public class ServiceNode {

    @Id
    private Integer serviceId; // 全局唯一标识，需手动维护或通过逻辑生成

    @Relationship(type = "INSTALLED_IN", direction = Relationship.Direction.OUTGOING)
    private SpaceNode parentingSpace; // 所属空间，关系名自定义为 PROVIDED_IN

    private String serviceName; // 服务名称，例如“会议模式”

    private String serviceJson; // Node-RED 的 JSON 内容

    private String description; // 服务描述

}