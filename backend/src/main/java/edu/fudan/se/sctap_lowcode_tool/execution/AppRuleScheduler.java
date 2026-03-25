package edu.fudan.se.sctap_lowcode_tool.execution;

import edu.fudan.se.sctap_lowcode_tool.service.AppRuleExecutorService;
import edu.fudan.se.sctap_lowcode_tool.service.AppRuleService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@ConditionalOnProperty(name = "app.node-role", havingValue = "cloud", matchIfMissing = true)
public class AppRuleScheduler {
    private final AppRuleService appRuleService;
    private final AppRuleExecutorService appRuleExecutorService;

    @Autowired
    public AppRuleScheduler(AppRuleService appRuleService, AppRuleExecutorService appRuleExecutorService) {

        this.appRuleService = appRuleService;
        this.appRuleExecutorService = appRuleExecutorService;
    }

    @PostConstruct
    public void init() {
        log.info("【云端模式】定时调度器已启动，负责清理数据与检查超时等待...");
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // 每隔 1 小时执行一次清理任务，调用 cleanUpOldData 方法
        scheduler.scheduleAtFixedRate(appRuleService::cleanUpOldData, 0, 1, TimeUnit.HOURS);

        // 每隔 30 秒执行一次时间等待检查任务，调用 checkExpiredTimeWait 方法
        scheduler.scheduleAtFixedRate(appRuleExecutorService::checkExpiredTimeWait, 0, 30, TimeUnit.SECONDS);

        // 每隔 10 分钟执行一次动作等待检查任务，调用 checkExpiredActionWait 方法
        scheduler.scheduleAtFixedRate(appRuleExecutorService::checkExpiredActionWait, 0, 10, TimeUnit.MINUTES);
    }
}
