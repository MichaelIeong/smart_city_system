package edu.fudan.se.sctap_lowcode_tool;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import edu.fudan.se.sctap_lowcode_tool.config.MilvusConfig;
import edu.fudan.se.sctap_lowcode_tool.controller.AppRuleController;
import edu.fudan.se.sctap_lowcode_tool.execution.AppRuleExecutor;
import edu.fudan.se.sctap_lowcode_tool.service.AppRuleService;
import edu.fudan.se.sctap_lowcode_tool.utils.milvus.MilvusUtil;

@SpringBootApplication
@ComponentScan(excludeFilters = {
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MilvusConfig.class),
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MilvusUtil.class),
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AppRuleService.class),
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AppRuleController.class),
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AppRuleExecutor.class)

})
public class SctapLowCodeToolApplication {

	public static void main(String[] args) {
		SpringApplication.run(SctapLowCodeToolApplication.class, args);
	}

}
