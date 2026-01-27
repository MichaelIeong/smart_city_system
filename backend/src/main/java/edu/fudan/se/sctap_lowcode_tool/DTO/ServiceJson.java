package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class ServiceJson implements Serializable {
    private Object compositionJson;
    private Object totalJson;
    private Object deviceTypeArray;
}
