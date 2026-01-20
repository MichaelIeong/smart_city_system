package edu.fudan.se.sctap_lowcode_tool.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class ThreadPoolConfig {

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

    // 用于事件融合引擎中 Grouper(分组器) 及 Matcher(匹配器) 的线程池
    @Bean(name = "fusionEngineMatcher")
    public Executor fusionEngineMatcher() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(500);
        executor.setKeepAliveSeconds(0);
        executor.setThreadNamePrefix("fusion-match-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    // 用于事件融合引擎中 Executor(执行器) 的线程池
    @Bean(name = "fusionEngineExecutor")
    public Executor fusionEngineExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("fusion-exec-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean(name = "tslScheduledFetcher")
    public Executor tslScheduledFetcher() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(10);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("fusion-tsl-fetcher-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }


}
