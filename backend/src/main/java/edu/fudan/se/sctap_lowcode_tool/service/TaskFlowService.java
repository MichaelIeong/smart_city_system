package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.execution.CompositeServiceParser;
import edu.fudan.se.sctap_lowcode_tool.execution.CompositeServiceDispatcher;
import edu.fudan.se.sctap_lowcode_tool.execution.ServiceGraph; // 引入图对象
import edu.fudan.se.sctap_lowcode_tool.model.EnvService;
import edu.fudan.se.sctap_lowcode_tool.repository.EnvServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class TaskFlowService {

    @Autowired
    private EnvServiceRepository envServiceRepository;

    @Autowired
    private CompositeServiceParser parser;

    @Autowired
    private CompositeServiceDispatcher dispatcher; // 换成我们新写的执行器


    public List<String> call_service(String serviceName, Map<String, Object> params) {
        try {
            // 1. 复用之前写的 executeByName 异步方法
            // 2. 使用 .join() 变成同步等待，拿回 List<String>
            return this.executeByName(serviceName, params).join();
        } catch (Exception e) {
            // 如果出错，返回错误日志
            String timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm:ss"));
            return java.util.List.of(String.format("[ERROR]-[%s]: 执行失败 - %s", timestamp, e.getMessage()));
        }
    }
    /**
     * 通过名字执行服务，并透传外部参数
     */
    public CompletableFuture<List<String>> executeByName(String serviceName, Map<String, Object> params) throws Exception {
        // 1. 按名字从数据库找数据
        EnvService info = envServiceRepository.findByServiceName(serviceName);
        if (info == null) {
            throw new RuntimeException("数据库中未找到名为 [" + serviceName + "] 的服务配置");
        }

        // 2. 获取 JSON 字符串 (注意：确保你数据库存 JSON 的字段名是对的，这里是 ruleJson)
        String ruleJson = info.getRuleJson();
        
        // 3. 解析 JSON 得到“服务图”
        ServiceGraph graph = parser.parse(ruleJson);
        
        // 4. 将“图”和“参数”丢给派发器，并获取它返回的 Future 对象
        // 这个 Future 最终会包含所有的执行日志
        return dispatcher.dispatch(graph, params);
    }
}
