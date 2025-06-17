package edu.fudan.se.sctap_lowcode_tool.DTO.app;

import lombok.Data;

@Data
public class IgnoreStep implements ChainStep {

    private Ignore ignore;

    @Override
    public String getType() {
        return "ignore";
    }

    @Data
    public static class Ignore {

        private String event_type;

        private String location;
    }
}
