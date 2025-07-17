package edu.fudan.se.sctap_lowcode_tool.DTO.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.Map;

@Data
public class Condition {
    private Left left;

    private String operator;

    private String right;

    @Data
    @JsonDeserialize(using = LeftDeserializer.class)
    public static class Left {

        private String value;

        @JsonProperty("func")
        private Func func;

        // 判断是否是函数类型
        public boolean isFunction() {
            return func != null;
        }
    }

    @Data
    public static class Func {
        private String func;
        private Map<String, String> params;
    }
}
