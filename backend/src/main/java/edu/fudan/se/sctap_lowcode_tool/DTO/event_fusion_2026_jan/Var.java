package edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan;

import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Var 变量
 * @param key 变量名。变量的唯一标识符。
 * @param type 数据类型。
 * @param desc 描述。(可选)对变量的简要说明，一般是中文。
 */
public record Var(
    @NotNull String key, // 变量名。变量的唯一标识符。
    @NotNull VarType type, // 数据类型。
    @Nullable String desc // 描述。(可选)对变量的简要说明，一般是中文。
) {

    /**
     * 根据提供的 expr 构造 Param
     * @param expr 计算表达式 (SpEL)。计算该参数值的 SpEL 表达式，具体写法见文档。
     * @return 根据提供的 expr 构造的 Param 对象
     */
    public Param toParam(@NotNull String expr) {
        return new Param(this.key, this.type, this.desc, expr);
    }

    /**
     * 根据提供的 value 构造 Arg
     * @param value 变量的实际值。
     * @return 根据提供的 value 构造的 Arg 对象
     */
    public Arg toArg(@NotNull Object value) {
        return new Arg(this.key, this.type, this.desc, value);
    }
}
