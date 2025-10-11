package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.Data;
import java.util.List;

@Data
public class DeviceTypeWithFunctionsDTO {
    private Integer deviceTypeId;
    private String deviceTypeName;
    private Boolean isSensor;
    private List<ActuatingFunctionDTO> functions;

    @Data
    public static class ActuatingFunctionDTO {
        private Integer actuatingFunctionId;
        private String functionName;
        private String description;
    }
}
