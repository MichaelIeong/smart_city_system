package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceConfig;
import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceState;
import edu.fudan.se.sctap_lowcode_tool.DTO.StateTransition;
import edu.fudan.se.sctap_lowcode_tool.model.DeviceConfiguration;
import edu.fudan.se.sctap_lowcode_tool.repository.LHARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class LHAService {

    @Autowired
    private LHARepository lhaRepository;

    public Optional<DeviceConfiguration> findConfigurationById(int deviceId){
        return lhaRepository.findById(deviceId);
    }

    public void updateConfiguration(DeviceConfiguration deviceConfiguration){
        lhaRepository.save(deviceConfiguration);
    }

    public String generateLHA(String configuration) {
        StringBuilder lhaString = new StringBuilder();

        try {
            // Step 1: 解析 JSON 字符串到 DeviceConfig 对象
            DeviceConfig deviceConfig = DeviceConfig.fromJson(configuration);

            // Step 2: 生成LHA模型头部信息
            lhaString.append("LHA Model for: ").append(deviceConfig.getDeviceName()).append("\n");

            // Step 3: 处理每个状态并生成LHA状态
            List<DeviceState> states = deviceConfig.getStates();
            for (DeviceState deviceState : states) {
                // 添加状态的方程信息
                lhaString.append("State: ").append(deviceState.getStateName()).append("\n");
                lhaString.append("Equation: ").append(deviceState.getEquation()).append("\n");

                // Step 4: 处理状态转移，并生成对应的转换
                List<StateTransition> transitions = deviceState.getTransitions();
                for (StateTransition transition : transitions) {
                    lhaString.append("  Transition to ").append(transition.getToState()).append("\n");
                    lhaString.append("  Condition: ").append(transition.getCondition()).append("\n");
                }
                lhaString.append("\n");  // 每个状态后空一行
            }

            // Step 5: 返回构建的LHA模型字符串
        } catch (IOException e) {
            e.printStackTrace();
            return "Error: Failed to parse JSON configuration.";
        }

        return lhaString.toString();
    }
}
