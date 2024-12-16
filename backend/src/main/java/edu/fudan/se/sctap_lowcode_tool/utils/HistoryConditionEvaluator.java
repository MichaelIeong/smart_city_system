package edu.fudan.se.sctap_lowcode_tool.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.fudan.se.sctap_lowcode_tool.DTO.AppRuleContent;
import edu.fudan.se.sctap_lowcode_tool.DTO.AppRuleRunRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.BadRequestException;
import edu.fudan.se.sctap_lowcode_tool.model.EventHistory;

import java.util.List;
import java.util.Map;

public abstract class HistoryConditionEvaluator {

    public static HistoryConditionEvaluator dispatch(AppRuleContent.ConditionEvaluator conditionEvaluator) {
        //noinspection SwitchStatementWithTooFewBranches
        return switch (conditionEvaluator.name()) {
            case "atLeast" -> atLeast(conditionEvaluator.params());
            default -> throw new IllegalArgumentException("Unknown condition evaluator: " + conditionEvaluator.name());
        };
    }

    public static HistoryConditionEvaluator atLeast(Map<String, Object> params)
        throws BadRequestException {

        int leastCount;
        Map<String, DynamicValue> conditions;

        try {
            leastCount = Double.valueOf(params.get("count").toString()).intValue();
        } catch (NumberFormatException e) {
            throw new BadRequestException(
                "400", "count should be an int",
                "count", params.toString(), "HistoryConditionEvaluator.atLeast");
        }

        try {
            conditions = new Gson().fromJson(
                new Gson().toJson(params.get("conditions")),
                new TypeToken<Map<String, DynamicValue>>() {}.getType()
                                            );

        } catch (Exception e) {
            throw new BadRequestException(
                "400", "condition should be a Map<String, DynamicValue>",
                "condition", params.toString(), "HistoryConditionEvaluator.atLeast"
            );
        }

        return new HistoryConditionEvaluator() {
            @Override
            public boolean eval(List<EventHistory> histories, AppRuleRunRequest request) {
                // 将Condition的动态值固定
                Map<String, String> fixedConditions = DynamicValue.getActualArgs(conditions, request.eventData());

                int count = 0;
                for (EventHistory history : histories) {
                    // 将history event data转换为Map
                    Map<String, String> historyData = new Gson().fromJson(
                        history.getEventData(),
                        new TypeToken<Map<String, String>>() {}.getType()
                                                                         );
                    // 确保fixedConditions是history event data的一个子集
                    boolean satisfy = true;
                    for (var entry : fixedConditions.entrySet()) {
                        if (!historyData.get(entry.getKey()).equals(entry.getValue())) {
                            satisfy = false;
                            break;
                        }
                    }
                    if (satisfy) {
                        count++;
                    }
                }
                return count >= leastCount;
            }
        };
    }

    public abstract boolean eval(List<EventHistory> histories, AppRuleRunRequest request);

}
