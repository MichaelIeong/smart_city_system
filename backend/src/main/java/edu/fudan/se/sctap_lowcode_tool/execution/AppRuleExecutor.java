package edu.fudan.se.sctap_lowcode_tool.execution;

import edu.fudan.se.sctap_lowcode_tool.controller.AppRuleController;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class AppRuleExecutor {
    private final AppRuleController appRuleController;

    @Autowired
    public AppRuleExecutor(AppRuleController appRuleController) {
        this.appRuleController = appRuleController;
    }

    @PostConstruct
    public void init() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // 每隔 1 小时执行一次清理任务
        // 调用 cleanUpOldData 方法
        scheduler.scheduleAtFixedRate(appRuleController::cleanUpOldData, 0, 1, TimeUnit.HOURS);
    }
}
