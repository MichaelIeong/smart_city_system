package edu.fudan.se.sctap_lowcode_tool.DTO.app;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class LeftDeserializer extends JsonDeserializer<Condition.Left> {
    @Override
    public Condition.Left deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        var node = jsonParser.getCodec().readTree(jsonParser);
        Condition.Left left = new Condition.Left();
        // 文本类型
        if(node.isValueNode()) {
            left.setValue(node.toString());
        }
        else {
            Condition.Func func = jsonParser.getCodec().treeToValue(node, Condition.Func.class);
            left.setFunc(func);
        }
        return left;
    }
}
