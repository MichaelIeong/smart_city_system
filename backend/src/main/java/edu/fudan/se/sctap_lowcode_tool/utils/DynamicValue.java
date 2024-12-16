package edu.fudan.se.sctap_lowcode_tool.utils;

import java.util.Map;
import java.util.stream.Collectors;

public record DynamicValue(
    ValueType valueType,
    String value // for face value, it is the value itself;
    // for request value, it is the key of the value in the request
) {

    public static Map<String, String> getActualArgs(Map<String, DynamicValue> args, Map<String, String> variables) {
        return args.entrySet().stream().collect(
            Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().getActualValue(variables)
                            )
                                               );
    }

    public String getActualValue(Map<String, String> variables) {
        if (valueType == ValueType.FACE_VALUE) {
            return value;
        } else {
            return variables.get(value);
        }
    }

    public enum ValueType {
        FACE_VALUE,
        REQUEST_VALUE
    }

}
