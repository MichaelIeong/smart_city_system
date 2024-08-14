package edu.fudan.se.sctap_lowcode_tool.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
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

}

