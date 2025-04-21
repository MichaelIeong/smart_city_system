package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.Operator;
import edu.fudan.se.sctap_lowcode_tool.utils.Operators.BasicOperatorUtil;
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
        // 1) 原有的数值比较
        utilOperators.put("Greater than", (input1, input2) ->
                BasicOperatorUtil.greaterThan(toDouble(input1), toDouble(input2))
        );
        utilOperators.put("Less than", (input1, input2) ->
                BasicOperatorUtil.lessThan(toDouble(input1), toDouble(input2))
        );
        utilOperators.put("Equal to", (input1, input2) ->
                BasicOperatorUtil.equalTo(toDouble(input1), toDouble(input2))
        );
        utilOperators.put("Greater than or equal to", (input1, input2) ->
                BasicOperatorUtil.greaterThanOrEqualTo(toDouble(input1), toDouble(input2))
        );
        utilOperators.put("Less than or equal to", (input1, input2) ->
                BasicOperatorUtil.lessThanOrEqualTo(toDouble(input1), toDouble(input2))
        );

        // 2) 原有的布尔运算（不带时间戳）
        utilOperators.put("AND", (input1, input2) ->
                BasicOperatorUtil.and(toBoolean(input1), toBoolean(input2))
        );
        utilOperators.put("OR", (input1, input2) ->
                BasicOperatorUtil.or(toBoolean(input1), toBoolean(input2))
        );

        // 3) **新增：带时间戳版本的 AND/OR，调用 andTime() / orTime()**
        utilOperators.put("AND_TIME", (input1, input2) -> {
            Map<String, Object> map1 = castToMap(input1);
            Map<String, Object> map2 = castToMap(input2);
            // 拿到布尔值
            Boolean bool1 = toBoolean(map1.get("value"));
            Boolean bool2 = toBoolean(map2.get("value"));
            // 拿到时间戳
            Long ts1 = toLong(map1.get("timestamp"));
            Long ts2 = toLong(map2.get("timestamp"));
            // 拿到最大时间差
            Long maxTimeDiff = toLong(map1.get("maxTimeDiff"));

            // **这里改成调用 andTime(...) 而不是原先的 and(...)**
            return BasicOperatorUtil.andTime(bool1, ts1, bool2, ts2, maxTimeDiff);
        });

        utilOperators.put("OR_TIME", (input1, input2) -> {
            Map<String, Object> map1 = castToMap(input1);
            Map<String, Object> map2 = castToMap(input2);
            Boolean bool1 = toBoolean(map1.get("value"));
            Boolean bool2 = toBoolean(map2.get("value"));
            Long ts1 = toLong(map1.get("timestamp"));
            Long ts2 = toLong(map2.get("timestamp"));
            Long maxTimeDiff = toLong(map1.get("maxTimeDiff"));

            // **这里改成调用 orTime(...) 而不是原先的 or(...)**
            return BasicOperatorUtil.orTime(bool1, ts1, bool2, ts2, maxTimeDiff);
        });
    }

    /**
     * 获取所有工具类运算符（封装为 Operator 对象）。
     */
    public List<Operator> getAllUtilOperators() {
        // 直接调用 OperatorUtil 提供的封装方法
        return BasicOperatorUtil.getAllUtilOperators();
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
     * 将输入对象转换为 Long 类型
     */
    private Long toLong(Object input) {
        if (input instanceof Number) {
            return ((Number) input).longValue();
        }
        try {
            return Long.parseLong(input.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("输入值无法转换为 Long：" + input);
        }
    }

    /**
     * 将输入对象强制转换为 Map<String,Object>
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> castToMap(Object obj) {
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        } else {
            throw new IllegalArgumentException("输入值不是 Map 类型：" + obj);
        }
    }

    /**
     * Functional Interface for operator logic
     */
    @FunctionalInterface
    private interface OperatorFunction {
        boolean apply(Object input1, Object input2);
    }
}