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
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

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

    // Jackson ObjectMapper，用于将消息加上timestamp后转为 JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 构造函数，由 Spring 自动注入 Kafka 地址和订阅的 Topic
     */
    public KafkaConsumerUtil(@Value("${kafka.bootstrap.servers}") String bootstrapServers,
                             @Value("${kafka.topic}") String topic) {
        this.topic = topic;

        // 配置 Kafka 消费者
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("group.id", "sc_uos_consumer_group");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");

        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(Collections.singletonList(topic));
    }

    /**
     * 启动消费者线程
     */
    @EventListener(ContextRefreshedEvent.class)
    public void startConsumer() {
        running.set(true);
        consumerThread = new Thread(this);
        consumerThread.start();
    }

    /**
     * 作为 Runnable 的 run 方法。
     * 当运行此方法时，会一直持续消费，直到调用 close()。
     */
    @Override
    public synchronized void run() {
        try {
            while (running.get()) {
                // 从 Kafka 拉取消息
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                if (records.isEmpty()) {
                    continue;
                }

                for (ConsumerRecord<String, String> record : records) {
                    // 1) 获取 Kafka 消息自带的 timestamp (可能是生产者发送时间或LogAppendTime)
                    long kafkaTimestamp = record.timestamp();

                    // 2) 尝试把原 value 解析成 JSON，如果失败则创建一个空 JSON 再填上原字符串
                    JsonNode originalJson;
                    try {
                        originalJson = objectMapper.readTree(record.value());
                    } catch (Exception e) {
                        // 解析失败，就手动创建一个包含原字符串的 JSON
                        ObjectNode fallbackJson = objectMapper.createObjectNode();
                        fallbackJson.put("value", record.value());
                        originalJson = fallbackJson;
                    }

                    // 3) 在原 JSON 上加一个 "timestamp" 字段
                    if (originalJson.isObject()) {
                        ((ObjectNode) originalJson).put("timestamp", kafkaTimestamp);
                    } else {
                        // 如果原 JSON 不是 object 类型，就包装一下
                        ObjectNode wrapper = objectMapper.createObjectNode();
                        wrapper.set("data", originalJson);
                        wrapper.put("timestamp", kafkaTimestamp);
                        originalJson = wrapper;
                    }

                    // 4) 将修改后的 JSON 转为字符串，放入队列
                    messageQueue.offer(originalJson.toString());
                }
            }
        } catch (WakeupException e) {
            if (running.get()) {
                throw e;
            }
        } catch (Exception e) {
            // 其它异常
        } finally {
            consumer.close();
        }
    }

    /**
     * 安全停止消费并关闭 Consumer
     */
    public synchronized void close() {
        running.set(false);
        consumer.wakeup();
    }

    /**
     * 在 Spring 上下文关闭时安全关闭消费者
     */
    @EventListener(ContextClosedEvent.class)
    public void stopConsumer() {
        close();
        try {
            if (consumerThread != null) {
                consumerThread.join();
            }
        } catch (InterruptedException e) {
            // ignore
        }
    }

    /**
     * 获取从 Kafka 消费的最新消息（此时已包含 timestamp 字段）
     *
     * @return 最新的消息（带 "timestamp" 的 JSON 字符串）
     */
    public String getLatestMessage() {
        String message = messageQueue.poll();
        if (message != null) {
            System.out.println("KafkaConsumerUtil: 获取到消息: " + message);
        } else {
            System.out.println("KafkaConsumerUtil: 当前没有可用的消息。");
        }
        return message;
    }
}