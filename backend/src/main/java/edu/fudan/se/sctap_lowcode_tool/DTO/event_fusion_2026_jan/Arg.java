package edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan;

import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Arg 实参
 * <p>
 * 带有 <b>实际值(value)</b> 的变量，存储该变量的实际值。
 *
 * @param key 变量名。变量的唯一标识符。
 * @param type 数据类型。
 * @param desc 描述。(可选)对变量的简要说明，一般是中文。
 * @param value 变量的实际值。
 */
public record Arg(
    @NotNull String key, // 变量名。变量的唯一标识符。
    @NotNull VarType type, // 数据类型。
    @Nullable String desc, // 描述。(可选)对变量的简要说明，一般是中文。
    @NotNull Object value // 变量的实际值。
) {
}
