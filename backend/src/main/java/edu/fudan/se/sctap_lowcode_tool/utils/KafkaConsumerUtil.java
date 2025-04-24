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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class KafkaConsumerUtil implements Runnable {

    private final KafkaConsumer<String, String> consumer;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final String topic;
    private Thread consumerThread;
    private final ConcurrentMap<Integer, String> latestMessages = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KafkaConsumerUtil(@Value("${kafka.bootstrap.servers}") String bootstrapServers,
                             @Value("${kafka.topic}") String topic) {
        this.topic = topic;

        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);

        // 使用隨機 group.id 避免 offset 汙染
        props.put("group.id", "sc_uos_consumer_group_" + UUID.randomUUID());

        props.put("enable.auto.commit", "false"); // 不提交 offset
        props.put("auto.offset.reset", "latest"); // 若無 offset，則從最新開始
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        props.put("reconnect.backoff.ms", "5000");
        props.put("reconnect.backoff.max.ms", "60000");
        props.put("retry.backoff.ms", "1000");

        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(Collections.singletonList(topic));

        // 手動 seek 到 partition 結尾
        consumer.poll(Duration.ofMillis(0)); // 觸發 partition assignment
        consumer.assignment().forEach(tp -> consumer.seekToEnd(Collections.singleton(tp)));
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startConsumerAfterAppReady() {
        consumerThread = new Thread(() -> {
            try {
                System.out.println("✅ KafkaConsumerUtil 启动中...");
                running.set(true);
                run();
            } catch (Exception e) {
                System.err.println("❌ KafkaConsumerUtil 启动失败：" + e.getMessage());
            }
        }, "kafka-consumer-thread");
        consumerThread.start();
    }

    @Override
    public synchronized void run() {
        try {
            while (running.get()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        JsonNode json = objectMapper.readTree(record.value());
                        ((ObjectNode) json).put("timestamp", record.timestamp());

                        JsonNode idNode = json.get("id");
                        if (idNode != null && idNode.canConvertToInt()) {
                            int sensorId = idNode.asInt();
                            latestMessages.put(sensorId, json.toString());
                        }
                    } catch (Exception e) {
                        System.err.println("❗ KafkaConsumerUtil: JSON 解析失败: " + e.getMessage());
                    }
                }
            }
        } catch (WakeupException e) {
            if (running.get()) throw e;
        } finally {
            consumer.close();
            System.out.println("🛑 KafkaConsumerUtil 已关闭 consumer");
        }
    }

    @EventListener(ContextClosedEvent.class)
    public void stopConsumer() {
        System.out.println("⏹ KafkaConsumerUtil 正在停止...");
        close();
        try {
            if (consumerThread != null) {
                consumerThread.join();
            }
        } catch (InterruptedException ignored) {
        }
    }

    public synchronized void close() {
        running.set(false);
        consumer.wakeup();
    }

    /**
     * 获取指定 sensorId 的最新消息
     *
     * @param sensorId 目标传感器 ID
     * @return 最新 JSON 消息（字符串），否则返回 null
     */
    public String getLatestMessageBySensorId(int sensorId) {
        return latestMessages.get(sensorId);
    }
}