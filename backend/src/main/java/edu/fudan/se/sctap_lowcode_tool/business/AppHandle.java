package edu.fudan.se.sctap_lowcode_tool.business;

import edu.fudan.se.sctap_lowcode_tool.bean.AppData;
import edu.fudan.se.sctap_lowcode_tool.bean.DSL;
import edu.fudan.se.sctap_lowcode_tool.bean.ScenarioAction;
import edu.fudan.se.sctap_lowcode_tool.bean.ScenarioTrigger;

/**
 * @author ：sunlinyue
 * @date ：Created in 2024/5/16 16:20
 * @description：处理application的具体执行
 * @modified By：
 * @version: $
 */
public class AppHandle {

    private AppData application;

    /**
     * app执行
     * @param appData
     */
    public void appExecute(AppData appData){
        application = appData;
        DSL dsl = application.getDsl();
        ScenarioTrigger scenarioTrigger = dsl.getScenarioTrigger();
        if(scenarioTrigger.getEvent_type().equals("Temperature_Change")){
            temperatureChange();
        }
        ScenarioAction scenarioAction = dsl.getScenarioAction().get(0);
        if (scenarioAction.getAction().equals("")){

        }
    }

    /**
     * 温度获取
     */
    public void temperatureChange(){
        String filter = application.getDsl().getScenarioTrigger().getFilter().get(0);
        String location = filter.split(" ")[2];
        //根据location查找这个空间中的温度数据，应该是从url直接调用
    }

    /**
     * SDK调用
     */
}
