package edu.fudan.se.sctap_lowcode_tool.model.event_fusion_2026_jan.common_operator;

import edu.fudan.se.sctap_lowcode_tool.DTO.BadRequestException;
import edu.fudan.se.sctap_lowcode_tool.DTO.ErrorResponse;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.Var;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.VarType;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <h4>NumberCompare 数值比较算子</h4>
 * <p>
 * 根据给定的运算符比较两个数值，返回布尔结果。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class NumberCompare extends CommonOperator {

    public static final String NUMBER_1 = "number1";
    public static final String OP = "op";
    public static final String NUMBER_2 = "number2";
    public static final String RESULT = "result";
    private final ConversionService conversionService;

    @NotNull
    @Override
    public Map<String, Object> calculate(@NotNull Map<String, Object> input) throws BadRequestException {
        Object number1 = input.get(NUMBER_1);
        Object number2 = input.get(NUMBER_2);
        if (number1 == null) {
            throw new BadRequestException(
                "400", "NumberCompare 算子输入参数有误 (缺少参数)",
                "input", input.toString(), "NumberCompare 算子缺少必要的输入参数: " + NUMBER_1
            );
        } else if (number2 == null) {
            throw new BadRequestException(
                "400", "NumberCompare 算子输入参数有误 (缺少参数)",
                "input", input.toString(), "NumberCompare 算子缺少必要的输入参数: " + NUMBER_2
            );
        }
        BigDecimal x;
        BigDecimal y;
        try {
            x = conversionService.convert(number1, BigDecimal.class);
            y = conversionService.convert(number2, BigDecimal.class);
            if (x == null || y == null) throw new IllegalArgumentException();
        } catch (ConversionException | IllegalArgumentException e) {
            throw new BadRequestException(
                "400", "NumberCompare 算子输入参数有误 (类型转换失败)",
                List.of(
                    new ErrorResponse.ErrorDetail(NUMBER_1, Objects.toString(number1), "要比较的两操作数必须是数值类型，至少有一个无法转换为数字。"),
                    new ErrorResponse.ErrorDetail(NUMBER_2, Objects.toString(number2), "要比较的两操作数必须是数值类型，至少有一个无法转换为数字。")
                )
            );
        }
        CompareOp op = CompareOp.fromString(Objects.toString(input.get(OP), null));
        return op.compare(x, y) ? Map.of(RESULT, true) : Map.of(RESULT, false);
    }

    private enum CompareOp {
        EQ {
            @Override
            public boolean compare(@NotNull BigDecimal x, @NotNull BigDecimal y) {
                return x.compareTo(y) == 0;
            }
        }, NE {
            @Override
            public boolean compare(@NotNull BigDecimal x, @NotNull BigDecimal y) {
                return x.compareTo(y) != 0;
            }
        }, GT {
            @Override
            public boolean compare(@NotNull BigDecimal x, @NotNull BigDecimal y) {
                return x.compareTo(y) > 0;
            }
        }, LT {
            @Override
            public boolean compare(@NotNull BigDecimal x, @NotNull BigDecimal y) {
                return x.compareTo(y) < 0;
            }
        }, GTE {
            @Override
            public boolean compare(@NotNull BigDecimal x, @NotNull BigDecimal y) {
                return x.compareTo(y) >= 0;
            }
        }, LTE {
            @Override
            public boolean compare(@NotNull BigDecimal x, @NotNull BigDecimal y) {
                return x.compareTo(y) <= 0;
            }
        };
        abstract public boolean compare(@NotNull BigDecimal x, @NotNull BigDecimal y);

        public static CompareOp fromString(@Nullable String opStr) throws BadRequestException {
            try {
                if (opStr == null) throw new IllegalArgumentException();
                return CompareOp.valueOf(opStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(
                    "400", "NumberCompare 算子输入参数有误 (不支持的比较符)",
                    OP, opStr, "不支持的比较运算符: " + opStr
                );
            }
        }
    }

    @NotNull
    @Override
    public String getDescription() {
        return "数值比较算子(根据给定的运算符比较两个数值)";
    }

    @NotNull
    @Override
    public List<Var> getOutputSpec() {
        return List.of(new Var(
            RESULT,
            VarType.Boolean,
            "数值比较结果，true表示满足比较条件，false表示不满足。"
        ));
    }

    @NotNull
    @Override
    public List<Var> getInputSpec() {
        return List.of(
            new Var(NUMBER_1, VarType.Number, "要进行比较的左操作数。"),
            new Var(OP, VarType.String, "比较运算符，支持的运算符包括: EQ（等于），NE（不等于），GT（大于），LT（小于），GTE（大于等于），LTE（小于等于）。"),
            new Var(NUMBER_2, VarType.Number, "要进行比较的右操作数。")
        );
    }
}
