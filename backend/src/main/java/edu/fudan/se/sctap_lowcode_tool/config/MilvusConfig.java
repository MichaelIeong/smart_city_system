package edu.fudan.se.sctap_lowcode_tool.config;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MilvusConfig {
    @Value("${milvus.host}")
    private String host;
    @Value("${milvus.port}")
    private Integer port;

    @Bean
    public MilvusClientV2 milvusClientV2(){
        String uri = "http://"+host+":"+port;
        ConnectConfig connectConfig = ConnectConfig.builder()
                .uri(uri)
                .build();
        return new MilvusClientV2(connectConfig);
    }
}
