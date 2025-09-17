package edu.fudan.se.sctap_lowcode_tool.DTO.APPRULE;

import lombok.Data;
import java.util.Map;

@Data
public class WaitStep implements ChainStep{
    private Wait wait;

    @Override
    public String getType() {
        return "wait";
    }

    @Data
    public static class Wait {

        private TimeWait time_wait;

        private ActionWait action_wait;

        @Data
        public static class TimeWait {

            private String event_type;

            private Map<String, String> wait_params;

            private String duration;

            private String unit;
        }

        @Data
        public static class ActionWait {

            private String event_type;

            private Map<String, String> wait_params;
        }

        public boolean isTimeWait() {
            return time_wait != null;
        }

        public boolean isActionWait() {
            return action_wait != null;
        }
    }
}
