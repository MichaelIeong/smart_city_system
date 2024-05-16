package edu.fudan.se.sctap_lowcode_tool.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author ：sunlinyue
 * @date ：Created in 2024/5/15 21:33
 * @description：
 * @modified By：
 * @version: $
 */
public class DSL {

    @JsonProperty("Scenario_Trigger")
    private ScenarioTrigger scenarioTrigger;

    @JsonProperty("Scenario_Action")
    private List<ScenarioAction> scenarioAction;

    @JsonProperty("Scenario_Description")
    private ScenarioDescription scenarioDescription;

    public DSL() {}

    public DSL(ScenarioTrigger scenarioTrigger, List<ScenarioAction> scenarioAction, ScenarioDescription scenarioDescription) {
        this.scenarioTrigger = scenarioTrigger;
        this.scenarioAction = scenarioAction;
        this.scenarioDescription = scenarioDescription;
    }

    public ScenarioTrigger getScenarioTrigger() {
        return scenarioTrigger;
    }

    public void setScenarioTrigger(ScenarioTrigger scenarioTrigger) {
        this.scenarioTrigger = scenarioTrigger;
    }

    public List<ScenarioAction> getScenarioAction() {
        return scenarioAction;
    }

    public void setScenarioAction(List<ScenarioAction> scenarioAction) {
        this.scenarioAction = scenarioAction;
    }

    public ScenarioDescription getScenarioDescription() {
        return scenarioDescription;
    }

    public void setScenarioDescription(ScenarioDescription scenarioDescription) {
        this.scenarioDescription = scenarioDescription;
    }

    @Override
    public String toString() {
        return "DSL{" +
                "scenarioTrigger=" + scenarioTrigger +
                ", scenarioAction=" + scenarioAction +
                ", scenarioDescription=" + scenarioDescription +
                '}';
    }
}

