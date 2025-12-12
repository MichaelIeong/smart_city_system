package edu.fudan.se.sctap_lowcode_tool.init;

import edu.fudan.se.sctap_lowcode_tool.SctapLowCodeToolApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(SctapLowCodeToolApplication.class);
	}

}
