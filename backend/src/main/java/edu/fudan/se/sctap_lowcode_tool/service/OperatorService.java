package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.Operator;
import edu.fudan.se.sctap_lowcode_tool.utils.OperatorUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OperatorService: 提供工具类运算符和动态运算逻辑。
 */
@Service
public class OperatorService {

    // 定义工具类运算符及其对应的逻辑映射
    private final Map<String, OperatorFunction> utilOperators = new HashMap<>();

    /**
     * 构造函数初始化工具类运算符逻辑
     */
    public OperatorService() {
        utilOperators.put("Greater than", (input1, input2) ->
            OperatorUtil.greaterThan(toDouble(input1), toDouble(input2))
        );
        utilOperators.put("Less than", (input1, input2) ->
            OperatorUtil.lessThan(toDouble(input1), toDouble(input2))
        );
        utilOperators.put("Equal to", (input1, input2) ->
            OperatorUtil.equalTo(toDouble(input1), toDouble(input2))
        );
        utilOperators.put("Greater than or equal to", (input1, input2) ->
            OperatorUtil.greaterThanOrEqualTo(toDouble(input1), toDouble(input2))
        );
        utilOperators.put("Less than or equal to", (input1, input2) ->
            OperatorUtil.lessThanOrEqualTo(toDouble(input1), toDouble(input2))
        );
        utilOperators.put("AND", (input1, input2) ->
            OperatorUtil.and(toBoolean(input1), toBoolean(input2))
        );
        utilOperators.put("OR", (input1, input2) ->
            OperatorUtil.or(toBoolean(input1), toBoolean(input2))
        );
    }

    /**
     * 获取所有工具类运算符（封装为 Operator 对象）。
     *
     * @return 工具类运算符的列表
     */
    public List<Operator> getAllUtilOperators() {
        // 直接调用 OperatorUtil 提供的封装方法
        return OperatorUtil.getAllUtilOperators();
    }

    /**
     * 根据运算符名称调用相应逻辑
     *
     * @param operatorName 运算符名称
     * @param input1       第一个输入值
     * @param input2       第二个输入值
     * @return 运算结果
     */
    public boolean applyUtilOperator(String operatorName, Object input1, Object input2) {
        if (!utilOperators.containsKey(operatorName)) {
            throw new UnsupportedOperationException("不支持的运算符: " + operatorName);
        }
        return utilOperators.get(operatorName).apply(input1, input2);
    }

    /**
     * 将输入对象转换为 Double 类型
     *
     * @param input 输入对象
     * @return 转换后的 Double 值
     */
    private Double toDouble(Object input) {
        if (input instanceof Number) {
            return ((Number) input).doubleValue();
        }
        try {
            return Double.parseDouble(input.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("输入值无法转换为数字：" + input);
        }
    }

    /**
     * 将输入对象转换为 Boolean 类型
     *
     * @param input 输入对象
     * @return 转换后的 Boolean 值
     */
    private Boolean toBoolean(Object input) {
        if (input instanceof Boolean) {
            return (Boolean) input;
        } else if (input instanceof Number) {
            return ((Number) input).doubleValue() != 0.0;
        }
        return Boolean.parseBoolean(input.toString());
    }

    /**
     * Functional Interface for operator logic
     */
    @FunctionalInterface
    private interface OperatorFunction {
        boolean apply(Object input1, Object input2);
    }
}