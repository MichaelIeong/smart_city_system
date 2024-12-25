package edu.fudan.se.sctap_lowcode_tool.utils;

import edu.fudan.se.sctap_lowcode_tool.model.Operator;

import java.util.ArrayList;
import java.util.List;

public class OperatorUtil {

    /**
     * 判断第一个值是否大于第二个值
     *
     * @param value1 第一个输入值
     * @param value2 第二个输入值
     * @return 返回第一个值是否大于第二个值
     */
    public static boolean greaterThan(Double value1, Double value2) {
        if (value1 == null || value2 == null) {
            throw new IllegalArgumentException("输入值不能为空");
        }
        return value1 > value2;
    }

    /**
     * 判断第一个值是否小于第二个值
     *
     * @param value1 第一个输入值
     * @param value2 第二个输入值
     * @return 返回第一个值是否小于第二个值
     */
    public static boolean lessThan(Double value1, Double value2) {
        if (value1 == null || value2 == null) {
            throw new IllegalArgumentException("输入值不能为空");
        }
        return value1 < value2;
    }

    /**
     * 判断两个值是否相等
     *
     * @param value1 第一个输入值
     * @param value2 第二个输入值
     * @return 返回两个值是否相等
     */
    public static boolean equalTo(Double value1, Double value2) {
        if (value1 == null || value2 == null) {
            throw new IllegalArgumentException("输入值不能为空");
        }
        return value1.equals(value2);
    }

    /**
     * 判断第一个值是否大于等于第二个值
     *
     * @param value1 第一个输入值
     * @param value2 第二个输入值
     * @return 返回第一个值是否大于等于第二个值
     */
    public static boolean greaterThanOrEqualTo(Double value1, Double value2) {
        if (value1 == null || value2 == null) {
            throw new IllegalArgumentException("输入值不能为空");
        }
        return value1 >= value2;
    }

    /**
     * 判断第一个值是否小于等于第二个值
     *
     * @param value1 第一个输入值
     * @param value2 第二个输入值
     * @return 返回第一个值是否小于等于第二个值
     */
    public static boolean lessThanOrEqualTo(Double value1, Double value2) {
        if (value1 == null || value2 == null) {
            throw new IllegalArgumentException("输入值不能为空");
        }
        return value1 <= value2;
    }

    /**
     * 判断两个布尔值是否满足 AND 逻辑运算。
     * 即：两个布尔值都为 true 时，返回 true。
     *
     * @param value1 第一个布尔值
     * @param value2 第二个布尔值
     * @return 如果两个值都为 true，返回 true；否则返回 false。
     */
    public static boolean and(Boolean value1, Boolean value2) {
        if (value1 == null || value2 == null) {
            throw new IllegalArgumentException("输入值不能为空");
        }
        return value1 && value2;
    }

    /**
     * 判断两个布尔值是否满足 OR 逻辑运算。
     * 即：两个布尔值至少有一个为 true 时，返回 true。
     *
     * @param value1 第一个布尔值
     * @param value2 第二个布尔值
     * @return 如果两个值至少有一个为 true，返回 true；否则返回 false。
     */
    public static boolean or(Boolean value1, Boolean value2) {
        if (value1 == null || value2 == null) {
            throw new IllegalArgumentException("输入值不能为空");
        }
        return value1 || value2;
    }

    /**
     * 获取所有工具类运算符并封装为 Operator 对象
     *
     * @return 工具类运算符的列表
     */
    public static List<Operator> getAllUtilOperators() {
        List<Operator> operators = new ArrayList<>();

        // 定义工具类运算符
        operators.add(createOperator("Greater than", null, "Boolean", true));
        operators.add(createOperator("Less than", null, "Boolean", true));
        operators.add(createOperator("Equal to", null, "Boolean", true));
        operators.add(createOperator("Greater than or equal to", null, "Boolean", true));
        operators.add(createOperator("Less than or equal to", null, "Boolean", true));
        operators.add(createOperator("AND", null, "Boolean", true));
        operators.add(createOperator("OR", null, "Boolean", true));

        return operators;
    }

    /**
     * 创建工具类运算符对象
     *
     * @param operatorName   运算符名称
     * @param operatorApi    运算符API（工具类运算符通常为 null）
     * @param outputName     运算符输出的名称
     * @param requiredInput  是否需要输入
     * @return 封装的 Operator 对象
     */
    private static Operator createOperator(String operatorName, String operatorApi, String outputName, Boolean requiredInput) {
        Operator operator = new Operator();
        operator.setOperatorName(operatorName);
        operator.setOperatorApi(operatorApi);
        operator.setOutputName(outputName);
        operator.setRequiredInput(requiredInput);
        return operator;
    }
}