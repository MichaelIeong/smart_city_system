package edu.fudan.se.sctap_lowcode_tool.FusionApi;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Bean
    public Queue eventsQueue() {
        return new Queue("eventsQueue");
    }
}
