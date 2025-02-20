package edu.fudan.se.sctap_lowcode_tool.DTO;

import java.util.List;

public class DeviceState {
    private String stateName; //名称
    private String equation; //方程
    private List<StateTransition> transitions;//转移到其他几种状态的条件

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getEquation() {
        return equation;
    }

    public void setEquation(String equation) {
        this.equation = equation;
    }

    public List<StateTransition> getTransitions() {
        return transitions;
    }

    public void setTransitions(List<StateTransition> transitions) {
        this.transitions = transitions;
    }
}
