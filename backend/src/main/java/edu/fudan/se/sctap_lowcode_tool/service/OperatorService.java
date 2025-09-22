package edu.fudan.se.sctap_lowcode_tool.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.fudan.se.sctap_lowcode_tool.model.Operator;
import edu.fudan.se.sctap_lowcode_tool.utils.OperatorUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OperatorService: 提供工具类运算符和动态运算逻辑。
 */
@Service
public class OperatorService {

    // 定义工具类运算符及其对应的逻辑映射
    private final Map<String, OperatorFunction> utilOperators = new HashMap<>();

    // 供 Time_FILTER(Countdown) 记忆每个节点的倒计时起点（单位：秒）
    // key 建议传 nodeId，避免多个节点互相影响
    private final ConcurrentHashMap<String, Long> countdownStartSeconds = new ConcurrentHashMap<>();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 构造函数初始化工具类运算符逻辑
     */
    public OperatorService() {
        // 1) 数值比较（符号）
        utilOperators.put(OperatorUtil.GREATER_THAN, (input1, input2) ->
                OperatorUtil.greaterThan(toDouble(input1), toDouble(input2))
        );
        utilOperators.put(OperatorUtil.LESS_THAN, (input1, input2) ->
                OperatorUtil.lessThan(toDouble(input1), toDouble(input2))
        );
        utilOperators.put(OperatorUtil.EQUAL_TO, (input1, input2) ->
                OperatorUtil.equalTo(toDouble(input1), toDouble(input2))
        );
        utilOperators.put(OperatorUtil.GREATER_THAN_OR_EQUAL_TO, (input1, input2) ->
                OperatorUtil.greaterThanOrEqualTo(toDouble(input1), toDouble(input2))
        );
        utilOperators.put(OperatorUtil.LESS_THAN_OR_EQUAL_TO, (input1, input2) ->
                OperatorUtil.lessThanOrEqualTo(toDouble(input1), toDouble(input2))
        );

        // 2) 布尔运算（纯布尔）
        utilOperators.put(OperatorUtil.AND, (input1, input2) ->
                OperatorUtil.and(toBoolean(input1), toBoolean(input2))
        );
        utilOperators.put(OperatorUtil.OR, (input1, input2) ->
                OperatorUtil.or(toBoolean(input1), toBoolean(input2))
        );

        // 3) 统一时间过滤器：Time_FILTER（全部用秒；无 tolerance）
        // - input1: 配置对象（Map / JsonNode / JSON字符串 都可）
        // - input2: 节点唯一 key（建议传 nodeId；用于 COUNTDOWN 记忆起点）
        utilOperators.put(OperatorUtil.TIME_FILTER, (input1, input2) -> {
            Map<String, Object> cfg = normalizeConfig(input1);
            String mode = String.valueOf(cfg.getOrDefault("mode", "")).toUpperCase();
            long nowSec = System.currentTimeMillis() / 1000L;

            switch (mode) {
                case "COUNTDOWN": {
                    long durationSeconds = getLong(cfg.get("durationSeconds"), 0L);
                    if (durationSeconds <= 0) return false;
                    String key = String.valueOf(input2); // 一般传 nodeId
                    long start = countdownStartSeconds.computeIfAbsent(key, k -> nowSec);
                    return (nowSec - start) < durationSeconds;
                }
                case "RANGE": {
                    Long start = getNullableLong(cfg.get("windowStartSeconds"));
                    Long end = getNullableLong(cfg.get("windowEndSeconds"));
                    if (start == null || end == null) return false;
                    return nowSec >= start && nowSec <= end;
                }
                case "EXACT": {
                    Long target = getNullableLong(cfg.get("targetEpochSeconds"));
                    if (target == null) return false;
                    return nowSec == target; // 精确到秒，无容差
                }
                default:
                    // 未知模式
                    return false;
            }
        });
    }

    /**
     * 获取所有工具类运算符（封装为 Operator 对象）。
     */
    public List<Operator> getAllUtilOperators() {
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
        OperatorFunction fn = utilOperators.get(operatorName);
        if (fn == null) {
            throw new UnsupportedOperationException("不支持的运算符: " + operatorName);
        }
        return fn.apply(input1, input2);
    }

    /* ======================= 帮助方法 ======================= */

    private static Double toDouble(Object input) {
        if (input instanceof Number) return ((Number) input).doubleValue();
        try {
            return Double.parseDouble(String.valueOf(input));
        } catch (Exception e) {
            throw new IllegalArgumentException("输入值无法转换为数字：" + input);
        }
    }

    private static Boolean toBoolean(Object input) {
        if (input instanceof Boolean) return (Boolean) input;
        if (input instanceof Number) return ((Number) input).doubleValue() != 0.0;
        return Boolean.parseBoolean(String.valueOf(input));
    }

    private static Map<String, Object> normalizeConfig(Object cfg) {
        try {
            if (cfg instanceof Map) {
                // noinspection unchecked
                return (Map<String, Object>) cfg;
            } else if (cfg instanceof JsonNode jsonNode) {
                return MAPPER.convertValue(jsonNode, Map.class);
            } else if (cfg instanceof CharSequence) {
                return MAPPER.readValue(String.valueOf(cfg), Map.class);
            }
        } catch (Exception ignored) {
        }
        return new HashMap<>(); // 兜底
    }

    private static Long getNullableLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }

    private static long getLong(Object v, long defVal) {
        Long n = getNullableLong(v);
        return n == null ? defVal : n;
    }

    /**
     * Functional Interface for operator logic
     */
    @FunctionalInterface
    private interface OperatorFunction {
        boolean apply(Object input1, Object input2);
    }
}