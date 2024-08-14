package edu.fudan.se.sctap_lowcode_tool.FusionApi;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class EventListener {
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @RabbitListener(queues = "eventQueue")
    public void handleEvent(String event){
        executorService.submit(() -> processEvent(event));
    }

    private void processEvent(String event){
        //处理事件的逻辑
        //在app资源库中查找对应的app
        String eventType = event.type;

    }
}
