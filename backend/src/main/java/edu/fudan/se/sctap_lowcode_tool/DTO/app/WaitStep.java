package edu.fudan.se.sctap_lowcode_tool.DTO.app;

import lombok.Data;

import java.util.Map;

@Data
public class WaitStep implements ChainStep {

    private Wait wait;

    @Data
    public static class Wait {

        private TimeCondition time_condition;

        private ActionCondition action_condition;

        @Data
        public static class TimeCondition {

            private String event_type;

            private Map<String, String> params;

            private String duration;

            private String unit;
        }

        @Data
        public static class ActionCondition {

            private String event_type;

            private Map<String, String> params;
        }

        public boolean isTimeCondition() {
            return time_condition != null;
        }

        public boolean isActionCondition() {
            return action_condition != null;
        }
    }
}
