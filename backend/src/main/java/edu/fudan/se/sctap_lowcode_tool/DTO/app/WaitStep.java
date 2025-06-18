package edu.fudan.se.sctap_lowcode_tool.DTO.app;

import lombok.Data;

@Data
public class WaitStep implements ChainStep {

    private Wait wait;

    @Data
    public static class Wait {

        private TimeCondition time_condition;

        private ActionCondition action_condition;

        @Data
        public static class TimeCondition {

            private String duration;

            private String unit;
        }

        @Data
        public static class ActionCondition {

            private String event_type;

            private String location;
        }

        public boolean isTimeCondition() {
            return time_condition != null;
        }

        public boolean isActionCondition() {
            return action_condition != null;
        }
    }
}
