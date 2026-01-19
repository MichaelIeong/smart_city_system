package edu.fudan.se.sctap_lowcode_tool;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.fudan.se.sctap_lowcode_tool.DTO.BadRequestException;
import edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.common_operator.CommonOperatorRegistry;
import edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.common_operator.Count.CountCondition;
import edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.common_operator.Count.CountCondition.FieldType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.common_operator.Count.CountCondition.FieldType.*;
import static edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.common_operator.Count.CountCondition.Op.EQ;
import static edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.common_operator.Count.CountCondition.Op.GT;


@SpringBootTest
class CountOperatorTest {

    public static final String $_CAR_PLATE = "$.car.plate";
    public static final String $_CAR_WEIGHT = "$.car.weight";
    public static final String $_CAR_IS_TRUCK = "$.car.isTruck";
    public static final String TIME_WINDOW_SECONDS = "timeWindowSeconds";
    public static final String SPACE_EVENT_ID = "spaceEventId";
    public static final String COUNT_CONDITIONS = "countConditions";

    @Autowired
    private CommonOperatorRegistry commonOperatorRegistry;

    @Autowired
    private ObjectMapper objectMapper;

	private HashMap<String, Object> buildMap(
        Integer timeWindowSeconds,
        String spaceEventId,
        List<CountCondition> countConditions
    ) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(TIME_WINDOW_SECONDS, timeWindowSeconds);
        map.put(SPACE_EVENT_ID, spaceEventId);
        map.put(COUNT_CONDITIONS, objectMapper.convertValue(countConditions, new TypeReference<List<Map<String, Object>>>() {}));
        return map;
    }

    private HashMap<String, Object> buildStandardParams() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(TIME_WINDOW_SECONDS, 864000);
        map.put(SPACE_EVENT_ID, "truck_spill");
        List<Map<String, Object>> countConditions = new ArrayList<>();
        countConditions.add(buildCondition($_CAR_PLATE, String, EQ, "ABC123"));
        countConditions.add(buildCondition($_CAR_WEIGHT, Number, GT, "10"));
        countConditions.add(buildCondition($_CAR_IS_TRUCK, Boolean, EQ, "false"));
        map.put(COUNT_CONDITIONS, objectMapper.convertValue(countConditions, new TypeReference<List<Map<String, Object>>>() {}));
        return map;
    }

    private HashMap<String, Object> buildCondition(
        String jsonPath, FieldType type, CountCondition.Op op, String value
    ) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("jsonPath", jsonPath);
        map.put("type", type.toString());
        map.put("op", op.toString());
        map.put("value", value);
        return map;
    }

    private void test(Map<String, Object> params) {
        try {
            var result = commonOperatorRegistry.getOperator("Count").calculate(params);
            System.out.println(result);
        } catch (BadRequestException e) {
            var response = e.getErrorResponse();
            System.out.println("=====错误信息打印=====");
            System.out.println("错误信息: (" + response.errCode() + ") " + response.errMsg());
            if (!response.errDetails().isEmpty()) System.out.println("错误详情: ");
            response.errDetails().forEach(detail -> {
                System.out.println("+--- location: " + detail.location());
                System.out.println("     param: " + detail.param());
                System.out.println("     msg: " + detail.msg());
            });
        }
    }

    @Test
	void nullWindow() {
        var params = buildMap(null, "truck_spill", List.of());
        test(params);
    }

    @Test
    void nonExistWindow() {
        var params = buildMap(999999, "truck_spill", List.of());
        params.remove(TIME_WINDOW_SECONDS);
        test(params);
    }

    @Test
    void wrongTypeWindow() {
        var params = buildMap(999999, "truck_spill", List.of());
        params.put(TIME_WINDOW_SECONDS, "wrongType");
        test(params);
    }

    @Test
    void windowTooSmall() {
        var params = buildMap(1, "truck_spill", List.of());
        test(params);
    }

    @Test
    void nullEvent() {
        var params = buildMap(86400, null, List.of());
        test(params);
    }

    @Test
    void nonExistEvent() {
        var params = buildMap(86400, "non_exist_event", List.of());
        test(params);
    }

    @Test
    void zeroConditions() {
        var params = buildMap(86400, "truck_spill", List.of());
        test(params);
    }

    @Test
    void standardParams() {
        var params = buildStandardParams();
        test(params);
    }

    @Test
    void illegalJsonPath() {
        var params = buildStandardParams();
        params.put(COUNT_CONDITIONS, List.of(
            new CountCondition("illegal_json_path", String, EQ, "ABC123")
        ));
        test(params);
    }

    @Test
    void wrongTypeWithJsonPaths() {
        var params = buildStandardParams();
        params.put(COUNT_CONDITIONS, List.of(
            new CountCondition($_CAR_PLATE, FieldType.Number, EQ, "15")
        ));
        test(params);
    }

    @Test
    void unsupportedOp() {
        var params = buildStandardParams();
        params.put(COUNT_CONDITIONS, List.of(
            new CountCondition($_CAR_IS_TRUCK, Boolean, GT, "true")
        ));
        test(params);
    }

    @Test
    void wrongValueFormat() {
        var params = buildStandardParams();
        params.put(COUNT_CONDITIONS, List.of(
            new CountCondition($_CAR_WEIGHT, Number, EQ, "not_a_number")
        ));
        test(params);
    }

    @Test
    void wrongConditionFormat() {
        var standardParams = buildStandardParams();
        HashMap<String, Object> standardCondition = buildCondition($_CAR_PLATE, String, EQ, "ABC123");
        // conditions not list
        HashMap<String, Object> p1 = new HashMap<>(standardParams);
        p1.put("countConditions", standardCondition);
        test(p1);

        // condition missing op field
        var c2 = new HashMap<>(standardCondition);
        c2.remove("op");
        HashMap<String, Object> p2 = new HashMap<>(standardParams);
        p2.put("countConditions", List.of(c2));
        test(p2);

        // condition missing value field
        var c3 = new HashMap<>(standardCondition);
        c3.remove("value");
        HashMap<String, Object> p3 = new HashMap<>(standardParams);
        p3.put("countConditions", List.of(c3));
        test(p3);

        // condition with extra unknown field
        var c4 = new HashMap<>(standardCondition);
        c4.put("unknownField", "someValue");
        HashMap<String, Object> p4 = new HashMap<>(standardParams);
        p4.put("countConditions", List.of(c4));
        test(p4);

        // condition with wrong type for jsonPath field (map)
        var c5 = new HashMap<>(standardCondition);
        c5.put("jsonPath", Map.of("invalid", "jsonPath"));
        HashMap<String, Object> p5 = new HashMap<>(standardParams);
        p5.put("countConditions", List.of(c5));
        test(p5);

        // condition with wrong type for type field (integer)
        var c6 = new HashMap<>(standardCondition);
        c6.put("type", 123);
        HashMap<String, Object> p6 = new HashMap<>(standardParams);
        p6.put("countConditions", List.of(c6));
        test(p6);

        // condition with illegal enum for op field
        var c7 = new HashMap<>(standardCondition);
        c7.put("op", "ILLEGAL_OP");
        HashMap<String, Object> p7 = new HashMap<>(standardParams);
        p7.put("countConditions", List.of(c7));
        test(p7);
    }

}
