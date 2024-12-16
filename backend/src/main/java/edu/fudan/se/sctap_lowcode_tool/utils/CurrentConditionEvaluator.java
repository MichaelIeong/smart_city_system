package edu.fudan.se.sctap_lowcode_tool.utils;

public abstract class CurrentConditionEvaluator {

    /**
     * 从变量名获取值
     *
     * @param variable 变量名 形如: S01.P01 或 trigger_location.P02
     * @return 变量值
     */
    public abstract double get(String variable);

    // 解析并计算条件
    public boolean evaluateCondition(String condition) {
        // 替换所有空格，确保语法一致
        condition = condition.replace(" ", "");

        // 优先处理括号内的表达式
        while (condition.contains("(")) {
            int openIndex = condition.lastIndexOf('(');
            int closeIndex = condition.indexOf(')', openIndex);
            String innerCondition = condition.substring(openIndex + 1, closeIndex);
            boolean result = evaluateCondition(innerCondition);
            condition = condition.substring(0, openIndex) + result + condition.substring(closeIndex + 1);
        }

        // 分割条件，处理逻辑运算符 | 和 &
        String[] orConditions = condition.split("\\|");
        boolean result = false;
        for (String orCondition : orConditions) {
            boolean andResult = true;
            String[] andConditions = orCondition.split("&");
            for (String singleCondition : andConditions) {
                andResult &= evaluateSingleCondition(singleCondition);
            }
            result |= andResult;
        }
        return result;
    }

    // 计算单个条件
    private boolean evaluateSingleCondition(String condition) {
        // 解析条件中的操作符
        String[] operators = {">=", "<=", ">", "<", "="};
        for (String operator : operators) {
            int index = condition.indexOf(operator);
            if (index != -1) {
                String left = condition.substring(0, index);
                String right = condition.substring(index + operator.length());
                double leftValue = this.get(left);
                double rightValue = Double.parseDouble(right);
                switch (operator) {
                    case ">=":
                        return leftValue >= rightValue;
                    case "<=":
                        return leftValue <= rightValue;
                    case ">":
                        return leftValue > rightValue;
                    case "<":
                        return leftValue < rightValue;
                    case "=":
                        return leftValue == rightValue;
                }
            }
        }
        throw new IllegalArgumentException("Invalid condition: " + condition);
    }

}
