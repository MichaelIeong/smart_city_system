package edu.fudan.se.sctap_lowcode_tool.utils;

import edu.fudan.se.sctap_lowcode_tool.model.Operator;

import java.util.ArrayList;
import java.util.List;

public class OperatorUtil {

    /* ================== 运算符常量（统一用符号与简洁英文） ================== */
    public static final String GREATER_THAN = ">";
    public static final String LESS_THAN = "<";
    public static final String EQUAL_TO = "=";
    public static final String GREATER_THAN_OR_EQUAL_TO = ">=";
    public static final String LESS_THAN_OR_EQUAL_TO = "<=";

    public static final String AND = "And";
    public static final String OR = "Or";

    /**
     * 新增：统一的时间过滤器（倒计时 / 区间 / 精确时刻）
     */
    public static final String TIME_FILTER = "Time_FILTER";
    /* ===================================================================== */

    /**
     * 大于
     */
    public static boolean greaterThan(Double value1, Double value2) {
        if (value1 == null || value2 == null) {
            throw new IllegalArgumentException("输入值不能为空");
        }
        return value1 > value2;
    }

    /**
     * 小于
     */
    public static boolean lessThan(Double value1, Double value2) {
        if (value1 == null || value2 == null) {
            throw new IllegalArgumentException("输入值不能为空");
        }
        return value1 < value2;
    }

    /**
     * 等于
     */
    public static boolean equalTo(Double value1, Double value2) {
        if (value1 == null || value2 == null) {
            throw new IllegalArgumentException("输入值不能为空");
        }
        return value1.equals(value2);
    }

    /**
     * 大于等于
     */
    public static boolean greaterThanOrEqualTo(Double value1, Double value2) {
        if (value1 == null || value2 == null) {
            throw new IllegalArgumentException("输入值不能为空");
        }
        return value1 >= value2;
    }

    /**
     * 小于等于
     */
    public static boolean lessThanOrEqualTo(Double value1, Double value2) {
        if (value1 == null || value2 == null) {
            throw new IllegalArgumentException("输入值不能为空");
        }
        return value1 <= value2;
    }

    /**
     * And
     */
    public static boolean and(Boolean value1, Boolean value2) {
        if (value1 == null || value2 == null) {
            throw new IllegalArgumentException("输入值不能为空");
        }
        return value1 && value2;
    }

    /**
     * Or
     */
    public static boolean or(Boolean value1, Boolean value2) {
        if (value1 == null || value2 == null) {
            throw new IllegalArgumentException("输入值不能为空");
        }
        return value1 || value2;
    }

    /**
     * 工具运算符清单
     * 这里仅声明有哪些工具运算符；Time_FILTER 的实际判定在 FusionRuleService 中完成
     */
    public static List<Operator> getAllUtilOperators() {
        List<Operator> operators = new ArrayList<>();

        // 数值比较（符号）
        operators.add(createOperator(GREATER_THAN, null, "Boolean", true));
        operators.add(createOperator(LESS_THAN, null, "Boolean", true));
        operators.add(createOperator(EQUAL_TO, null, "Boolean", true));
        operators.add(createOperator(GREATER_THAN_OR_EQUAL_TO, null, "Boolean", true));
        operators.add(createOperator(LESS_THAN_OR_EQUAL_TO, null, "Boolean", true));

        // 布尔
        operators.add(createOperator(AND, null, "Boolean", false));
        operators.add(createOperator(OR, null, "Boolean", false));

        // 统一时间过滤器（需要输入一个配置对象）
        operators.add(createOperator(TIME_FILTER, null, "Boolean", true));

        return operators;
    }

    private static Operator createOperator(String operatorName, String operatorApi,
                                           String outputName, Boolean requiredInput) {
        Operator operator = new Operator();
        operator.setOperatorName(operatorName);
        operator.setOperatorApi(operatorApi);
        operator.setOutputName(outputName);
        operator.setRequiredInput(requiredInput);
        return operator;
    }
}