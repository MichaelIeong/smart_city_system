package edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * EventFusionRule 事件融合规则定义
 * <p>
 * 描述事件融合的完整规则，包括触发源、处理步骤、发布配置以及规则名称。
 * @param ruleName 事件融合规则名称。中文，用于前端展示。
 * @param triggers 触发器。列表，但当前只支持单触发源。
 * @param steps 算子步骤列表，定义 有向无环图 流程。
 * @param publish 事件发布配置。
 */
public record EventFusionRule(
	@NotBlank String ruleName,
	@Valid @NotNull List<Trigger> triggers,
	@Valid @NotNull List<Step> steps,
	@Valid @NotNull Publish publish
) {

	/**
	 * Trigger 触发器
	 * @param eventSource 事件类型，传感器事件或环境事件。
	 * @param eventId 事件ID，全局唯一。
	 */
	public record Trigger(
		@NotNull EventSource eventSource,
		@NotBlank String eventId
	) { }

	/**
	 * Step 处理步骤节点
	 * @param stepId 步骤ID，规则内唯一。
	 * @param condition 返回 bool 的 SpEL 表达式，具体写法见文档。<br/>
     *                   若运行时判定结果为 false，则直接终止整个规则流程。<br/>
     *                   该字段可选，若不填写则视为该步骤直接执行，无需条件判断。
	 * @param next 后续步骤ID集合，空数组表示无后续。
	 * @param input 算子入参定义，使用 Param 描述。
	 * @param operatorType 算子类型，通用算子或服务算子。
	 * @param operatorName 内置算子名称，当 operatorType=common 时必填。
	 * @param operatorUrl 服务算子URL，当 operatorType=service 时必填。
	 * @param operatorHttpMethod 服务算子HTTP方法，当 operatorType=service 时必填。
	 * @param output 算子出参定义，使用 Var 描述。
	 */
	public record Step(
		@NotBlank String stepId,
		@Nullable String condition,
		@NotNull List<String> next,
		@Valid @NotNull List<Param> input,
		@NotNull OperatorType operatorType,
		@Nullable String operatorName,
		@Nullable String operatorUrl,
		@Nullable HttpMethod operatorHttpMethod,
		@Valid @NotNull List<Var> output
	) { }

	/**
	 * Publish 事件发布配置
	 * @param spaceEventId 发布事件唯一标识符，全局唯一。
	 * @param spaceEventName 发布事件名，中文，用于前端展示。
	 * @param spaceEventDesc 事件描述，可选。
	 * @param condition 返回 bool 的事件发布条件表达式 (SpEL)，具体写法见文档。<br/>
     *                  若运行时判定结果为 false ，则不发布事件。<br/>
     *                  该字段可选，若不填写则视为直接发布事件，无需条件判断。
	 * @param output 事件载荷，使用 Param 描述。
	 */
	public record Publish(
		@NotBlank String spaceEventId,
		@NotBlank String spaceEventName,
		@Nullable String spaceEventDesc,
		@Nullable String condition,
		@Valid @NotNull List<Param> output
	) { }

	/**
	 * EventSource 事件来源类型
	 */
	public enum EventSource {
        /** 传感器事件（TSL 提供）*/
        sensorEvent,
        /** 环境事件（融合得到）*/
		spaceEvent
	}

	/**
	 * OperatorType 算子类型
	 */
	public enum OperatorType {
		/** 通用算子 */
        common,
        /** 服务算子 */
		service
	}

	/**
	 * HttpMethod 服务算子 HTTP 方法
	 */
	public enum HttpMethod {
		GET, POST, PUT, DELETE
	}
}
