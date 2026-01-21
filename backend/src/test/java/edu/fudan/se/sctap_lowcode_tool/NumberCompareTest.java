package edu.fudan.se.sctap_lowcode_tool;

import edu.fudan.se.sctap_lowcode_tool.DTO.BadRequestException;
import edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.common_operator.NumberCompare;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.support.DefaultConversionService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NumberCompareTest {

    private NumberCompare numberCompare;

    @BeforeEach
    void setUp() {
        // 注入实现
        DefaultConversionService conversionService = new DefaultConversionService();
        numberCompare = new NumberCompare(conversionService);
    }

    @Test
    void testCalculate_WithRealConversion() {
        // 测试 String 和 Integer 混合输入
        Map<String, Object> input = Map.of(
            NumberCompare.NUMBER_1, "123.45", // String 类型
            NumberCompare.NUMBER_2, 100,      // Integer 类型
            NumberCompare.OP, "GT"
        );
        Map<String, Object> result = numberCompare.calculate(input);
        assertEquals(true, result.get(NumberCompare.RESULT));
    }

    @Test
    void testCalculate_LessEqual() {
        // 测试 Integer 和 Double 混合输入 不同精度
        Map<String, Object> input = Map.of(
            NumberCompare.NUMBER_1, 50,
            NumberCompare.NUMBER_2, 50.0,
            NumberCompare.OP, "EQ"
        );
        Map<String, Object> result = numberCompare.calculate(input);
        assertEquals(true, result.get(NumberCompare.RESULT));
    }

    @Test
    void testCalculate_UnsupportedOperator() {
        // 测试不支持的运算符
        Map<String, Object> input = Map.of(
            NumberCompare.NUMBER_1, 100,
            NumberCompare.NUMBER_2, 50,
            NumberCompare.OP, "INVALID_OP"
        );
        try {
            numberCompare.calculate(input);
        } catch (Exception e) {
            assertEquals(BadRequestException.class, e.getClass());
            System.out.println("UnsupportedOperator Error: " + ((BadRequestException) e).getErrorResponse());
        }
    }

    @Test
    void testCalculate_InvalidNumberInput() {
        // 测试输入的不是数字
        Map<String, Object> input = Map.of(
            NumberCompare.NUMBER_1, "not_a_number",
            NumberCompare.NUMBER_2, "also_not_a_number",
            NumberCompare.OP, "EQ"
        );
        try {
            numberCompare.calculate(input);
        } catch (Exception e) {
            assertEquals(BadRequestException.class, e.getClass());
            System.out.println("InvalidNumberInput Error: " + ((BadRequestException) e).getErrorResponse());
        }
    }

    @Test
    void testCalculate_MissingOperator() {
        // 测试缺少运算符
        Map<String, Object> input = Map.of(
            NumberCompare.NUMBER_1, 100,
            NumberCompare.NUMBER_2, 50
        );
        try {
            numberCompare.calculate(input);
        } catch (Exception e) {
            assertEquals(BadRequestException.class, e.getClass());
            System.out.println("MissingOperator Error: " + ((BadRequestException) e).getErrorResponse());
        }
    }

    @Test
    void testCalculate_MissingNumber1() {
        // 测试缺少操作数
        Map<String, Object> input = Map.of(
            NumberCompare.NUMBER_2, 50,
            NumberCompare.OP, "GT"
        );
        try {
            numberCompare.calculate(input);
        } catch (Exception e) {
            assertEquals(BadRequestException.class, e.getClass());
            System.out.println("MissingNumber1 Error: " + ((BadRequestException) e).getErrorResponse());
        }
    }


}