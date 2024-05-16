package edu.fudan.se.sctap_lowcode_tool.bean;

/**
 * @author ：sunlinyue
 * @date ：Created in 2024/5/15 21:33
 * @description：
 * @modified By：
 * @version: $
 */
public class DSL {
    private ScenarioTrigger Scenario_Trigger;
    //private Scenario_Action Scenario_Action;
    //private ScenarioDescription Scenario_Description;

    public DSL() {}

    public DSL(ScenarioTrigger Scenario_Trigger) {
        this.Scenario_Trigger = Scenario_Trigger;
        //this.Scenario_Action = Scenario_Action;
        //this.Scenario_Description = Scenario_Description;
    }


    public ScenarioTrigger getScenario_Trigger() {
        return Scenario_Trigger;
    }

    public void setScenario_Trigger(ScenarioTrigger Scenario_Trigger) {
        this.Scenario_Trigger = Scenario_Trigger;
    }

//    public Scenario_Action getScenario_Action() {
//        return Scenario_Action;
//    }

//    public void setScenario_Action(Scenario_Action Scenario_Action) {
//        this.Scenario_Action = Scenario_Action;
//    }

//    public ScenarioDescription getScenarioDescription() {
//        return Scenario_Description;
//    }

//    public void setScenarioDescription(ScenarioDescription Scenario_Description) {
//        this.Scenario_Description = Scenario_Description;
//    }

    @Override
    public String toString() {
        return "DSL{" +
                "Scenario_Trigger=" + Scenario_Trigger +
                //", Scenario_Action=" + Scenario_Action +
//                ", scenarioDescription=" + Scenario_Description +
                '}';
    }
}

