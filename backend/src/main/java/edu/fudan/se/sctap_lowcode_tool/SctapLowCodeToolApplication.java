package edu.fudan.se.sctap_lowcode_tool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableJpaRepositories(
		basePackages = "edu.fudan.se.sctap_lowcode_tool.repository",
        transactionManagerRef = "jpaTransactionManager"
)
@EnableNeo4jRepositories(
		basePackages = "edu.fudan.se.sctap_lowcode_tool.neo4jRepository",
		transactionManagerRef = "neo4jTransactionManager"
)
public class SctapLowCodeToolApplication {

	public static void main(String[] args) {
		SpringApplication.run(SctapLowCodeToolApplication.class, args);
	}

}
