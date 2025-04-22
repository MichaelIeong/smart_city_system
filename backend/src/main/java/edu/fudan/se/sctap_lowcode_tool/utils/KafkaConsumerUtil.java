package edu.fudan.se.sctap_lowcode_tool.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class KafkaConsumerUtil implements Runnable {

    private final KafkaConsumer<String, String> consumer;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final String topic;
    private Thread consumerThread;
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KafkaConsumerUtil(@Value("${kafka.bootstrap.servers}") String bootstrapServers,
                             @Value("${kafka.topic}") String topic) {
        this.topic = topic;

        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("group.id", "sc_uos_consumer_group");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "latest");

        // 重连相关配置
        props.put("reconnect.backoff.ms", "5000");         // 第一次重连间隔：5秒
        props.put("reconnect.backoff.max.ms", "60000");    // 最大重连间隔：60秒
        props.put("retry.backoff.ms", "1000");             // 请求失败后的重试等待时间：1秒

        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(Collections.singletonList(topic));
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startConsumerAfterAppReady() {
        new Thread(() -> {
            try {
                System.out.println("✅ KafkaConsumerUtil 正在启动 consumer...");
                running.set(true);
                run(); // 启动消费线程
            } catch (Exception e) {
                System.err.println("❌ KafkaConsumerUtil 启动失败：" + e.getMessage());
            }
        }, "kafka-consumer-thread").start();
    }

    @Override
    public synchronized void run() {
        try {
            while (running.get()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                if (records.isEmpty()) continue;

                for (ConsumerRecord<String, String> record : records) {
                    long kafkaTimestamp = record.timestamp();
                    JsonNode originalJson;
                    try {
                        originalJson = objectMapper.readTree(record.value());
                    } catch (Exception e) {
                        ObjectNode fallbackJson = objectMapper.createObjectNode();
                        fallbackJson.put("value", record.value());
                        originalJson = fallbackJson;
                    }

                    if (originalJson.isObject()) {
                        ((ObjectNode) originalJson).put("timestamp", kafkaTimestamp);
                    } else {
                        ObjectNode wrapper = objectMapper.createObjectNode();
                        wrapper.set("data", originalJson);
                        wrapper.put("timestamp", kafkaTimestamp);
                        originalJson = wrapper;
                    }

                    messageQueue.offer(originalJson.toString());
                }
            }
        } catch (WakeupException e) {
            if (running.get()) throw e;
        } finally {
            consumer.close();
        }
    }

    @EventListener(ContextClosedEvent.class)
    public void stopConsumer() {
        close();
        try {
            if (consumerThread != null) {
                consumerThread.join();
            }
        } catch (InterruptedException ignored) {}
    }

    public synchronized void close() {
        running.set(false);
        consumer.wakeup();
    }

    public String getLatestMessageBySensorId(int sensorId, long timeoutMillis) {
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < timeoutMillis) {
            String message = messageQueue.poll();
            if (message != null) {
                try {
                    JsonNode json = objectMapper.readTree(message);
                    JsonNode idNode = json.get("id");
                    if (idNode != null && idNode.asInt() == sensorId) {
                        return message;
                    }
                } catch (Exception e) {
                    System.out.println("KafkaConsumerUtil: 消息解析失败：" + e.getMessage());
                }
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
            }
        }

        System.out.println("KafkaConsumerUtil: 超时未获取到 sensorId=" + sensorId + " 的消息");
        return null;
    }
}