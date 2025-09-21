package edu.fudan.se.sctap_lowcode_tool.utils;

import edu.fudan.se.sctap_lowcode_tool.model.Operator;

import java.util.ArrayList;
import java.util.List;

public class OperatorUtil {

    /* ================== 运算符常量（直接用符号） ================== */
    public static final String GREATER_THAN = ">";
    public static final String LESS_THAN = "<";
    public static final String EQUAL_TO = "=";
    public static final String GREATER_THAN_OR_EQUAL_TO = ">=";
    public static final String LESS_THAN_OR_EQUAL_TO = "<=";

    public static final String AND = "And";
    public static final String OR = "Or";

    public static final String AND_TIME = "And_TIME";
    public static final String OR_TIME = "Or_TIME";
    /* ========================================================== */

    /**
     * 判断第一个值是否大于第二个值
     */
    public static boolean greaterThan(Double value1, Double value2) {
        if (value1 == null || value2 == null) {
            throw new IllegalArgumentException("输入值不能为空");
        }
        return value1 > value2;
    }

    /**
     * 判断第一个值是否小于第二个值
     */
    public static boolean lessThan(Double value1, Double value2) {
        if (value1 == null || value2 == null) {
            throw new IllegalArgumentException("输入值不能为空");
        }
        return value1 < value2;
    }

    /**
     * 判断两个值是否相等
     */
    public static boolean equalTo(Double value1, Double value2) {
        if (value1 == null || value2 == null) {
            throw new IllegalArgumentException("输入值不能为空");
        }
        return value1.equals(value2);
    }

    /**
     * 判断第一个值是否大于等于第二个值
     */
    public static boolean greaterThanOrEqualTo(Double value1, Double value2) {
        if (value1 == null || value2 == null) {
            throw new IllegalArgumentException("输入值不能为空");
        }
        return value1 >= value2;
    }

    /**
     * 判断第一个值是否小于等于第二个值
     */
    public static boolean lessThanOrEqualTo(Double value1, Double value2) {
        if (value1 == null || value2 == null) {
            throw new IllegalArgumentException("输入值不能为空");
        }
        return value1 <= value2;
    }

    /**
     * AND 逻辑
     */
    public static boolean and(Boolean value1, Boolean value2) {
        if (value1 == null || value2 == null) {
            throw new IllegalArgumentException("输入值不能为空");
        }
        return value1 && value2;
    }

    /**
     * OR 逻辑
     */
    public static boolean or(Boolean value1, Boolean value2) {
        if (value1 == null || value2 == null) {
            throw new IllegalArgumentException("输入值不能为空");
        }
        return value1 || value2;
    }

    /*================== 新增：带时间戳的 AND_TIME / OR_TIME =================*/

    public static boolean andTime(Boolean value1, Long timestamp1,
                                  Boolean value2, Long timestamp2,
                                  Long maxTimeDiff) {
        if (value1 == null || value2 == null) {
            throw new IllegalArgumentException("[AND_TIME] 输入布尔值不能为空");
        }
        if (timestamp1 == null || timestamp2 == null || maxTimeDiff == null) {
            throw new IllegalArgumentException("[AND_TIME] 时间戳或最大时间差不能为空");
        }
        if (Math.abs(timestamp1 - timestamp2) > maxTimeDiff) {
            return false;
        }
        return value1 && value2;
    }

    public static boolean orTime(Boolean value1, Long timestamp1,
                                 Boolean value2, Long timestamp2,
                                 Long maxTimeDiff) {
        if (value1 == null || value2 == null) {
            throw new IllegalArgumentException("[OR_TIME] 输入布尔值不能为空");
        }
        if (timestamp1 == null || timestamp2 == null || maxTimeDiff == null) {
            throw new IllegalArgumentException("[OR_TIME] 时间戳或最大时间差不能为空");
        }
        if (Math.abs(timestamp1 - timestamp2) > maxTimeDiff) {
            return false;
        }
        return value1 || value2;
    }

    /**
     * 获取所有工具类运算符并封装为 Operator 对象
     */
    public static List<Operator> getAllUtilOperators() {
        List<Operator> operators = new ArrayList<>();

        operators.add(createOperator(GREATER_THAN, null, "Boolean", true));
        operators.add(createOperator(LESS_THAN, null, "Boolean", true));
        operators.add(createOperator(EQUAL_TO, null, "Boolean", true));
        operators.add(createOperator(GREATER_THAN_OR_EQUAL_TO, null, "Boolean", true));
        operators.add(createOperator(LESS_THAN_OR_EQUAL_TO, null, "Boolean", true));
        operators.add(createOperator(AND, null, "Boolean", false));
        operators.add(createOperator(OR, null, "Boolean", false));
        operators.add(createOperator(AND_TIME, null, "Boolean", true));
        operators.add(createOperator(OR_TIME, null, "Boolean", true));

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