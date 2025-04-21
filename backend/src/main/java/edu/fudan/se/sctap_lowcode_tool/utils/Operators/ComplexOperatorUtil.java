package edu.fudan.se.sctap_lowcode_tool.utils.Operators;

import edu.fudan.se.sctap_lowcode_tool.model.Operator;

import java.util.ArrayList;
import java.util.List;

public class ComplexOperatorUtil {


    public static boolean greaterThan(Double value1, Double value2) {
        if (value1 == null || value2 == null) {
            throw new IllegalArgumentException("输入值不能为空");
        }
        return value1 > value2;
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
        operators.add(createOperator("AND", null, "Boolean", false));
        operators.add(createOperator("OR", null, "Boolean", false));
        operators.add(createOperator("AND_TIME", null, "Boolean", true));
        operators.add(createOperator("OR_TIME", null, "Boolean", true));

        return operators;
    }

    /**
     * 创建工具类运算符对象
     *
     * @param operatorName  运算符名称
     * @param operatorApi   运算符API（工具类运算符通常为 null）
     * @param outputName    运算符输出的名称
     * @param requiredInput 是否需要输入
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