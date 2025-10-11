package edu.fudan.se.sctap_lowcode_tool.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ThreadPollConfig {

    @Bean(name = "appRuleExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);  // 核心线程数
        executor.setMaxPoolSize(10);  // 最大线程数
        executor.setQueueCapacity(100);  // 等待队列容量
        executor.setKeepAliveSeconds(60);  // 空闲线程最大存活时间
        executor.setThreadNamePrefix("app-rule-executor-");  // 线程名称前缀
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());  // 拒绝策略
        executor.initialize();
        return executor;
    }
}
