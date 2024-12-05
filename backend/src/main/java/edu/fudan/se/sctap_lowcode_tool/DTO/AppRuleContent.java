package edu.fudan.se.sctap_lowcode_tool.DTO;

import com.google.gson.annotations.SerializedName;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public record AppRuleContent(
        @SerializedName("Scenario_Trigger")
        ScenarioTrigger scenarioTrigger,
        @SerializedName("Scenario_Action")
        List<ScenarioAction> scenarioActions
) {

    public ScenarioAction getAction() {
        return scenarioActions().getFirst();
    }

    public record ScenarioTrigger(
            @SerializedName("event_type")
            List<List<String>> eventTypes,
            @SerializedName("filter")
            List<String> filters) {

        public int size() {
            return eventTypes.size();
        }

        public Optional<String> getEventType(int index) {
            var l = eventTypes.get(index);
            return l.isEmpty() ? Optional.empty() : Optional.of(l.getFirst());
        }

        public Optional<String> getSpaceIs(int index) {
            var filterString = filters.get(index); // location is S01,timestamp > 18:45:08
            return Arrays.stream(filterString.split(","))
                    .filter(s -> s.startsWith("location"))
                    .findFirst() // location is S01
                    .map(s -> s.split(" ")[2]); // S01
        }

        public Optional<String> getTimestampOp(int index) {
            var filterString = filters.get(index); // location is S01,timestamp > 18:45:08
            return Arrays.stream(filterString.split(","))
                    .filter(s -> s.startsWith("timestamp"))
                    .findFirst() // timestamp > 18:45:08
                    .map(s -> s.split(" ")[1]); // >
        }

        public Optional<LocalTime> getTimestampValue(int index) {
            var filterString = filters.get(index); // location is S01,timestamp > 18:45:08
            return Arrays.stream(filterString.split(","))
                    .filter(s -> s.startsWith("timestamp"))
                    .findFirst() // timestamp > 18:45:08
                    .map(s -> s.split(" ")[2]) // 18:45:08
                    .map(LocalTime::parse);
        }

        public boolean satisfyTimeCondition(int index) {
            var op = this.getTimestampOp(index);
            var targetTime = this.getTimestampValue(index);
            var nowTime = LocalTime.now();
            // 时间判断条件为空时, 视为满足条件
            if (op.isEmpty() || targetTime.isEmpty()) {
                return true;
            }
            // 时间判断条件不为空时, 进行判断
            // 只支持 >, <, = 三种操作符 (其他运算符视为不满足)
            return switch (op.get()) {
                case ">" -> nowTime.isAfter(targetTime.get());
                case "<" -> nowTime.isBefore(targetTime.get());
                case "=" -> nowTime.equals(targetTime.get());
                default -> false;
            };
        }

        public boolean satisfySpaceCondition(int index, String space) {
            var targetSpace = this.getSpaceIs(index);
            // 空间判断条件为空时, 视为满足条件
            // 空间判断条件不为空时, 进行判断
            return targetSpace.map(s -> s.equals(space)).orElse(true);
        }

    }

    public record ScenarioAction(
            @SerializedName("history_condition")
            String historyCondition,
            @SerializedName("current_condition")
            String currentCondition,
            @SerializedName("action")
            Action action
    ) {
        public record Action(
                @SerializedName("action_name")
                String actionName,
                @SerializedName("action_location")
                List<String> actionLocations,
                @SerializedName("action_param")
                String actionParam
        ) {
        }
    }
}
