package edu.fudan.se.sctap_lowcode_tool.util;

import edu.fudan.se.sctap_lowcode_tool.model.ServiceInfo;
import edu.fudan.se.sctap_lowcode_tool.neo4jModel.ServiceNode;
import edu.fudan.se.sctap_lowcode_tool.neo4jModel.SpaceNode;

public class ServiceConverterUtil {

    /**
     * 将 ServiceInfo 转为 ServiceNode（Neo4j 节点）
     * @param info MySQL 的 ServiceInfo 实体
     * @param neo4jServiceId Neo4j 使用的 serviceId（你自己生成）
     * @return 转换后的 ServiceNode 实例
     */
    public static ServiceNode convertToNode(ServiceInfo info, Integer neo4jServiceId) {
        if (info == null) return null;

        ServiceNode node = new ServiceNode();
        node.setServiceId(neo4jServiceId);  // 手动赋值

        node.setServiceName(info.getServiceName());
        node.setServiceJson(info.getServiceJson());
        node.setDescription(info.getDescription());

        if (info.getParentingSpace() != null) {
            SpaceNode spaceNode = new SpaceNode();
            spaceNode.setSpaceId(info.getParentingSpace().getSpaceId());
            node.setParentingSpace(spaceNode);
        }

        return node;
    }
}