package edu.fudan.se.sctap_lowcode_tool.DTO;

public class StateTransition {
    private String toState;//目标状态
    private String condition;//转换条件

    public String getToState() {
        return toState;
    }

    public void setToState(String toState) {
        this.toState = toState;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
