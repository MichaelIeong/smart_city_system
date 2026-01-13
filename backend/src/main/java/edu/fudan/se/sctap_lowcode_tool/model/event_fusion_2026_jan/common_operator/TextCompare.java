package edu.fudan.se.sctap_lowcode_tool.model.event_fusion_2026_jan.common_operator;

import edu.fudan.se.sctap_lowcode_tool.DTO.BadRequestException;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.Var;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.VarType;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <h4>TextCompare 文本比较算子</h4>
 * <p>
 * 比较给定的两个文本是否相等，返回布尔结果。
 * </p>
 */
@Service
public class TextCompare extends CommonOperator {

    public static final String RESULT = "result";
    public static final String TEXT_1 = "text1";
    public static final String TEXT_2 = "text2";

    @NotNull
    @Override
    public Map<String, Object> calculate(@NotNull Map<String, Object> input) throws BadRequestException {
        if (!input.containsKey(TEXT_1)) {
            throw new BadRequestException(
                "400", "TextCompare 算子输入参数有误",
                "input", input.toString(), "TextCompare 算子缺少必要的输入参数: text1"
            );
        } else if (!input.containsKey(TEXT_2)) {
            throw new BadRequestException(
                "400", "TextCompare 算子输入参数有误",
                "input", input.toString(), "TextCompare 算子缺少必要的输入参数: text2"
            );
        }
        String text1 = input.get(TEXT_1).toString();
        String text2 = input.get(TEXT_2).toString();
        return Map.of(RESULT, Objects.equals(text1, text2));
    }

    @NotNull
    @Override
    public String getDescription() {
        return "文本比较算子(比较给定的两个文本是否相等)";
    }

    @NotNull
    @Override
    public List<Var> getOutputSpec() {
        return List.of(new Var(
            RESULT,
            VarType.Boolean,
            "文本比较结果，true表示相等，false表示不相等。"
        ));
    }

    @NotNull
    @Override
    public List<Var> getInputSpec() {
        return List.of(
            new Var(TEXT_1, VarType.String, "要比较的第一个文本。"),
            new Var(TEXT_2, VarType.String, "要比较的第二个文本。")
        );
    }
}
