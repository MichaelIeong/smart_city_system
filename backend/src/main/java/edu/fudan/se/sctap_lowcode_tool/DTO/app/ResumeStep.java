package edu.fudan.se.sctap_lowcode_tool.DTO.app;

import lombok.Data;

@Data
public class ResumeStep implements ChainStep {

    private Resume resume;

    @Data
    public static class Resume {

        private String event_type;

        private String location;
    }
}
