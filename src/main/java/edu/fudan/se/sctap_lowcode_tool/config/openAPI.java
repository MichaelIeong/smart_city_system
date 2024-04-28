package edu.fudan.se.sctap_lowcode_tool.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;

public class openAPI {
    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("SCTAP Tool API")
                        .description("SCTAP Low-code Tool")
                        .version("v0.0.1"));
    }


}
