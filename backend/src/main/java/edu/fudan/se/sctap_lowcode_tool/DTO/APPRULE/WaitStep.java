package edu.fudan.se.sctap_lowcode_tool.DTO.APPRULE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WaitStep implements ChainStep{
    private Wait wait;

    @Override
    @JsonIgnore
    public String getType() {
        return "wait";
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
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

        @JsonIgnore
        public boolean isTimeWait() {
            return time_wait != null;
        }

        @JsonIgnore
        public boolean isActionWait() {
            return action_wait != null;
        }

        @JsonIgnore
        public String getWaitKey() {
            Map<String, String> waitParams;
            if (isTimeWait()) {
                waitParams = time_wait.wait_params;
            } else {
                waitParams = action_wait.wait_params;
            }
            return waitParams.entrySet().iterator().next().getKey();
        }
    }
}
