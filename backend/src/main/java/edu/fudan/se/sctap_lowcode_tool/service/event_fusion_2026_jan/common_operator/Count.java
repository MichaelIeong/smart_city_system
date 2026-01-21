package edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.common_operator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.fudan.se.sctap_lowcode_tool.DTO.BadRequestException;
import edu.fudan.se.sctap_lowcode_tool.DTO.ErrorResponse;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.Var;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.VarType;
import edu.fudan.se.sctap_lowcode_tool.model.event_fusion_2026_jan.SpaceEventHistory;
import edu.fudan.se.sctap_lowcode_tool.repository.SpaceEventHistoryRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import static java.lang.Boolean.parseBoolean;

/**
 * <h4>计数算子 Count</h4>
 * 
 * 基于 SpaceEventHistory 表，按事件 ID、时间窗口和 JSONPath 过滤条件统计事件数量。
 */
@Service
@RequiredArgsConstructor
public class Count extends CommonOperator {

    public static final String TIME_WINDOW_SECONDS = "timeWindowSeconds";
    public static final String SPACE_EVENT_ID = "spaceEventId";
    public static final String COUNT_CONDITIONS = "countConditions";
    public static final String COUNT = "count";

    private final SpaceEventHistoryRepository repository;
    private final ConversionService conversionService;
    private final ObjectMapper objectMapper;

    /**
     * 执行计算：按输入参数构建 Specification，并返回匹配的历史事件数量。
     * @param input 规则执行时传入的参数，包含 timeWindowSeconds、spaceEventId、countConditions
     * @return key 为 count 的 Map，值为匹配数量
     * @throws BadRequestException 输入缺失、类型不符或查询异常时抛出
     */
    @NotNull
    @Override
    public Map<String, Object> calculate(@NotNull Map<String, Object> input) throws BadRequestException {

        // 构建输入参数 (timeWindowSeconds, spaceEventId, countCondition)
        Integer timeWindowSecs = this.convert(input.get(TIME_WINDOW_SECONDS), Integer.class, "inputs.timeWindowSeconds");
        String spaceEventId = this.convert(input.get(SPACE_EVENT_ID), String.class, "inputs.spaceEventId");
        List<CountCondition> countConditions = this.parseCountConditions(input.get(COUNT_CONDITIONS));
        countConditions.forEach(this::validateCountCondition);

        // 构建 Specification
        Specification<SpaceEventHistory> spaceEventSpec = Specification.where(
            (root, query, cb) -> cb.equal(root.get(SPACE_EVENT_ID), spaceEventId)
        );
        Specification<SpaceEventHistory> timeWindowSpec = Specification.where(
            (root, query, cb) -> cb.greaterThanOrEqualTo(
                root.get("createdAt"),
                LocalDateTime.now().minusSeconds(timeWindowSecs)
            )
        );
        Specification<SpaceEventHistory> countConditionSpec = this.toSpecification(countConditions);
        Specification<SpaceEventHistory> combineSpec = spaceEventSpec.and(timeWindowSpec).and(countConditionSpec);

        // 执行查询并返回结果
        try {
            long count = repository.count(combineSpec);
            return Map.of(COUNT, count);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException(
                "400", "Count 算子执行失败",
                "inputs", input.toString(), "执行 Count 计算时发生错误: " + e.getClass().getSimpleName() + e.getMessage()
            );
        }
    }

    @NotNull
    @Override
    public String getDescription() {
        return "计数算子(用于在数据库中查询符合指定条件的环境事件数量)";
    }

    @NotNull
    @Override
    public List<Var> getOutputSpec() {
        return List.of(new Var(
            COUNT,
            VarType.Number,
            "在指定时间窗口内，符合条件的环境事件数量。"
        ));
    }

    @NotNull
    @Override
    public List<Var> getInputSpec() {
        return List.of(
            new Var(TIME_WINDOW_SECONDS, VarType.Number, "时间窗口（秒），只统计该时间段内(N秒前～现在)的事件数量。"),
            new Var(SPACE_EVENT_ID, VarType.String, "环境事件ID，只统计该环境事件的数量。"),
            new Var(COUNT_CONDITIONS, VarType.Array, "计数条件，指定对事件负载数据的过滤条件，条件间为AND关系，格式为 List<CountCondition>。")
        );
    }


    // =====================================================================
    // CountCondition 定义
    // =====================================================================

    /**
     * 单条计数条件：描述 payload 中的 JSONPath 字段、字段类型、比较操作符及目标值。
     * <p>在构建 Specification 时，字段类型会决定 JSON_VALUE 的返回类型，以及可用的操作符。</p>
     */
    public record CountCondition(
        @NotNull String jsonPath,
        @NotNull FieldType type,
        @NotNull Op op,
        @NotNull String value
    ) {

        @NotNull @Override
        public String toString() {
            return String.format("%s (as %s) %s '%s'", jsonPath, type.name(), op.symbol, value);
        }

        public enum Op {
            EQ("=="),
            NE("!="),
            GT(">"),
            GTE(">="),
            LT("<"),
            LTE("<="),
            LIKE("LIKE");
            public final String symbol;
            Op(String symbol) {this.symbol = symbol;}
        }

        /**
         * 字段类型：决定 JSON_VALUE 的返回类型以及允许的比较符号。
         */
        public enum FieldType {
            String {
                @Override
                public Expression<?> toExpression(Root<?> root, CriteriaBuilder cb, String jsonPath) {
                    return cb.function("JSON_VALUE", String.class, root.get("payload"), cb.literal(jsonPath));
                }
                @Override
                public Predicate toPredicate(CriteriaBuilder cb, Expression<?> expr, Op op, String value, String conditionStr) throws BadRequestException {
                    Expression<String> x = expr.as(String.class);
                    return switch (op) {
                        case EQ -> cb.equal(x, value);
                        case NE -> cb.notEqual(x, value);
                        case LIKE -> cb.like(x, value);
                        default -> throw new BadRequestException(
                            "400", "CountCondition 构建失败 (对字符串使用了不支持的操作符)",
                            List.of(
                                new ErrorResponse.ErrorDetail("op", op.toString(), "字符串类型仅支持 EQ, NE, LIKE 操作符"),
                                new ErrorResponse.ErrorDetail("expr", conditionStr, "")
                        ));
                    };
                }
            },
            Number {
                @Override
                public Expression<?> toExpression(Root<?> root, CriteriaBuilder cb, String jsonPath) {
                    return cb.function("JSON_VALUE", BigDecimal.class, root.get("payload"), cb.literal(jsonPath));
                }
                @Override
                public Predicate toPredicate(CriteriaBuilder cb, Expression<?> expr, Op op, String value, String conditionStr) {
                    Expression<BigDecimal> x = expr.as(BigDecimal.class);
                    BigDecimal v;
                    try {
                        v = new BigDecimal(value);
                    } catch (NumberFormatException e) {
                        throw new BadRequestException(
                            "400", "CountCondition 构建失败 (无法将 value 转换为数字)",
                            List.of(
                                new ErrorResponse.ErrorDetail("op", op.toString(), ""),
                                new ErrorResponse.ErrorDetail("expr", conditionStr, ""),
                                new ErrorResponse.ErrorDetail("value", value, "提供的类型为 Number, 但 value 无法转换为数字")
                            ));
                    }
                    return switch (op) {
                        case EQ -> cb.equal(x, v);
                        case NE -> cb.notEqual(x, v);
                        case GT -> cb.greaterThan(x, v);
                        case GTE -> cb.greaterThanOrEqualTo(x, v);
                        case LT -> cb.lessThan(x, v);
                        case LTE -> cb.lessThanOrEqualTo(x, v);
                        default -> throw new BadRequestException(
                            "400", "CountCondition 构建失败 (对数字使用了不支持的操作符)",
                            List.of(
                                new ErrorResponse.ErrorDetail("op", op.toString(), "数字类型仅支持 EQ, NE, GT, GTE, LT, LTE 操作符"),
                                new ErrorResponse.ErrorDetail("expr", conditionStr, "")
                            ));
                    };
                }
            },
            Boolean {
                @Override
                public Expression<?> toExpression(Root<?> root, CriteriaBuilder cb, String jsonPath) {
                    return cb.function("JSON_VALUE", String.class, root.get("payload"), cb.literal(jsonPath));
                }

                @Override
                public Predicate toPredicate(CriteriaBuilder cb, Expression<?> expr, Op op, String value,
                    String conditionStr
                ) {
                    Expression<String> x = expr.as(String.class);
                    String v = parseBoolean(value) ? "true" : "false";
                    return switch (op) {
                        case EQ -> cb.equal(x, v);
                        case NE -> cb.notEqual(x, v);
                        default -> throw new BadRequestException(
                            "400", "CountCondition 构建失败 (对布尔值使用了不支持的操作符)",
                            List.of(
                                new ErrorResponse.ErrorDetail("op", op.toString(), "布尔类型仅支持 EQ, NE 操作符"),
                                new ErrorResponse.ErrorDetail("expr", conditionStr, "")
                            ));
                    };
                }
            };

            /**
             * 指定该数据类型如何提取 payload 中指定 JSONPath 对应字段的表达式，用于后续 Predicate 构建。
             * @param root 实体根，指向 SpaceEventHistory
             * @param cb CriteriaBuilder，用于生成函数调用或比较表达式
             * @param jsonPath JSONPath 路径，如 $.field[0].subField
             * @return 代表该字段的 JPA Expression
             */
            public abstract Expression<?> toExpression(Root<?> root, CriteriaBuilder cb, String jsonPath);

            /**
             * 指定该数据类型如何将提取出的 Expression 按操作符和值转为 Predicate，供 Specification 组合使用。
             * @param cb CriteriaBuilder
             * @param expr 字段表达式（类型由枚举项决定）
             * @param op 比较操作符（EQ/NE/GT/...）
             * @param value 目标值，字符串形式，会在方法内按需要转换
             * @param conditionStr 便于报错的条件描述
             * @return 对应的 Predicate
             * @throws BadRequestException 当操作符与类型不匹配或 value 解析失败时
             */
            public abstract Predicate toPredicate(CriteriaBuilder cb, Expression<?> expr, Op op, String value, String conditionStr) throws BadRequestException;
        }
    }


    // =====================================================================
    // 将输入参数解析为 CountCondition
    // =====================================================================
    /**
     * 通用类型转换并包装异常，补充错误位置信息，要求输入非空且可被 ConversionService 转换。
     */
    private <T> T convert(@Nullable Object inputValue, Class<T> targetType, String location) throws BadRequestException {
        try {
            if (inputValue == null) {
                throw new BadRequestException(
                    "400", "Count 算子输入参数有误 (缺少必要的输入参数)",
                    location, "null", "缺少必要的输入参数"
                );
            }
            return conversionService.convert(inputValue, targetType);
        } catch (ConversionException e) {
            throw new BadRequestException(
                "400", "Count 算子输入参数有误 (类型转换失败)",
                location, Objects.toString(inputValue), "该参数应为 " + targetType.getSimpleName() + " 类型, 但提供的值无法转换为该类型"
            );
        }
    }

    /**
     * 将外部传入的 countConditions 反序列化为 CountCondition 列表，转换失败时返回详细错误。
     */
    private List<CountCondition> parseCountConditions(@Nullable Object inputValue) throws BadRequestException {
        try {
            if (inputValue == null) {
                throw new BadRequestException(
                    "400", "Count 算子输入参数有误 (缺少必要的输入参数)",
                    "inputs.countConditions", "null", "缺少必要的输入参数"
                );
            }
            return objectMapper.convertValue(inputValue, new TypeReference<>() {});
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                "400", "Count 算子输入参数有误 (类型转换失败)",
                List.of(
                    new ErrorResponse.ErrorDetail("inputs.countConditions", Objects.toString(inputValue), "该参数应为: List<CountCondition> 类型, 但提供的值无法转换为该类型"),
                    new ErrorResponse.ErrorDetail("exception", e.getClass().getSimpleName(), e.getMessage())
                )
            );
        }
    }


    // =====================================================================
    // 校验 CountCondition 合法性
    // =====================================================================

    /**
     * 校验单条 CountCondition 的必填字段与 JSONPath 合法性。
     */
    private void validateCountCondition(CountCondition condition) throws BadRequestException {
        List.of(
            Map.entry("jsonPath", condition.jsonPath),
            Map.entry("type", condition.type),
            Map.entry("op", condition.op),
            Map.entry("value", condition.value)
        ).forEach(p -> {
            String key = p.getKey();
            Object value = p.getValue();
            if (value == null) {
                throw new BadRequestException(
                    "400", "CountCondition 构建失败 (缺少必要的字段)",
                    List.of(
                        new ErrorResponse.ErrorDetail("condition." + key, "null", "CountCondition 的 " + key + " 字段不能为空"),
                        new ErrorResponse.ErrorDetail("condition", condition.toString(), "")
                    )
                );
            }
        });
        validatePath(condition.jsonPath);
    }

    private static final Pattern JSON_PATH = Pattern.compile(
        "^\\$(\\.[A-Za-z_][A-Za-z0-9_]*)*(\\[[0-9]+])*(\\.[A-Za-z_][A-Za-z0-9_]*)*(\\[[0-9]+])*$"
    );

    /**
     * 校验 JSONPath 格式是否符合 $.a.b[0].c 形式，不合法时抛 BadRequestException。
     */
    private static void validatePath(String path) throws BadRequestException {
        if (path == null || !JSON_PATH.matcher(path).matches()) {
            throw new BadRequestException(
                "400", "CountCondition 构建失败 (无效的 JSONPath 格式)",
                "jsonPath", path, "JSONPath 必须符合规范，例如 $.fieldName 或 $.fieldName[0].subField"
            );
        }
    }


    // =====================================================================
    // 将 CountCondition 转换为 JPA Specification
    // =====================================================================

    /**
     * 将 CountCondition 列表转换为单个 Specification，所有条件以 AND 相连。
     */
    private Specification<SpaceEventHistory> toSpecification(List<CountCondition> conditions) throws BadRequestException {
        return (root, query, cb) -> {
            List<Predicate> predicates = conditions.stream().map(cond -> {
                Expression<?> expr = cond.type().toExpression(root, cb, cond.jsonPath());
                return cond.type().toPredicate(cb, expr, cond.op(), cond.value(), cond.toString());
            }).toList();
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
