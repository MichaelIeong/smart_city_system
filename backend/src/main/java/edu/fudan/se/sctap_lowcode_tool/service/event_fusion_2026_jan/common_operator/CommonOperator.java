package edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.common_operator;

import edu.fudan.se.sctap_lowcode_tool.DTO.BadRequestException;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.Var;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * CommonOperator 内置通用算子抽象类
 * <p>
 * 所有内置通用算子都必须继承此抽象类并实现其方法。
 * 通用算子用于事件融合规则的处理步骤中，对输入数据进行计算处理并返回输出结果。
 * </p>
 */
public abstract class CommonOperator {

    /**
     * <b>给定输入时执行计算，返回计算结果。</b>
     * <p>
     * 根据输入参数进行数据处理，并返回计算结果。该方法在事件融合规则执行时被调用。
     * </p>
     * 
     * <h4>实现要求：</h4>
     * <ul>
     *   <li>计算输入应与 {@link #getInputSpec()} 的定义一致</li>
     *   <li>计算输出应与 {@link #getOutputSpec()} 的定义一致</li>
     *   <li>应当处理输入参数缺失或类型不匹配的情况，抛出 {@link BadRequestException} </li>
     * </ul>
     * 
     * <h4>注意事项：</h4>
     * <ul>
     *   <li>此方法应当是纯函数，不应修改输入参数</li>
     *   <li>对于耗时操作，应考虑性能影响</li>
     * </ul>
     * 
     * @param input 输入参数 Map，key 为变量名，value 为变量值
     * @return 输出结果 Map，key 为变量名，value 为计算结果
     * @throws BadRequestException 当输入参数不符合预期时
     */
    abstract public @NotNull Map<String, Object> calculate(@NotNull Map<String, Object> input) throws BadRequestException;

    /**
     * 获取算子的简要描述
     * <p>
     * 提供该算子的功能简介，便于用户理解其用途。
     * </p>
     *
     * @return 算子描述字符串，不可为 null
     */
    abstract public @NotNull String getDescription();

    /**
     * 获取算子的输出变量规范
     * <p>
     * 定义该算子执行后会产生哪些输出变量及其类型。
     * </p>
     * 
     * <h4>实现要求：</h4>
     * <ul>
     *   <li>返回的 {@link Var} 列表中，每个变量名必须唯一</li>
     *   <li>变量名应当具有描述性，采用驼峰命名法</li>
     *   <li>变量类型应当准确反映 {@link #calculate(Map)} 方法实际返回的数据类型</li>
     * </ul>
     * 
     * @return 输出变量规范列表，不可为 null，可以为空列表（表示无输出）
     */
    abstract public @NotNull List<Var> getOutputSpec();

    /**
     * 获取算子的输入变量规范
     * <p>
     * 定义该算子需要哪些输入变量及其类型。
     * 在规则配置时，前端会根据此规范提示用户提供相应的输入参数。
     * </p>
     * 
     * <h4>实现要求：</h4>
     * <ul>
     *   <li>返回的 {@link Var} 列表中，每个变量名必须唯一</li>
     *   <li>变量类型应当准确反映 {@link #calculate(Map)} 方法期望的输入数据类型</li>
     *   <li>所有必需的输入变量都应在此列表中声明</li>
     * </ul>
     * 
     * @return 输入变量规范列表，不可为 null，可以为空列表（表示无需输入）
     */
    abstract public @NotNull List<Var> getInputSpec();
}
