package edu.fudan.se.sctap_lowcode_tool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = {"edu.fudan.se.sctap_lowcode_tool.repository"})
public class SctapLowCodeToolApplication {

	public static void main(String[] args) {
		SpringApplication.run(SctapLowCodeToolApplication.class, args);
	}

}
