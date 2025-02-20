package edu.fudan.se.sctap_lowcode_tool.utils;

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

    /**
     * 构造函数，由 Spring 自动注入 Kafka 地址和订阅的 Topic
     *
     * @param bootstrapServers Kafka 地址
     * @param topic Kafka 订阅的 Topic 名称
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

                // 如果没有新消息，则跳过
                if (records.isEmpty()) {
                    continue;
                }

                // 打印每条获取到的消息信息
                for (ConsumerRecord<String, String> record : records) {
                    // 将获取到的消息放入队列
                    messageQueue.offer(record.value());
                }
            }
        } catch (WakeupException e) {
            // 如果是正常关闭，跳出循环
            if (running.get()) {
                throw e;
            }
        } catch (Exception e) {
            // 捕获异常时不打印异常内容（已移除打印）
        } finally {
            consumer.close();
        }
    }

    /**
     * 安全停止消费并关闭 Consumer
     */
    public synchronized void close() {
        running.set(false);
        consumer.wakeup(); // 会触发 WakeupException，从而跳出消费循环
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
            // 这里也不再打印异常
        }
    }

    /**
     * 获取从 Kafka 消费的最新消息
     *
     * @return 最新的消息
     */
    public String getLatestMessage() {
        // 打印获取消息的操作
        String message = messageQueue.poll();  // 获取并移除队列中的最新消息
        if (message != null) {
            System.out.println("KafkaConsumerUtil: 获取到消息: " + message);  // 只在此处打印
        } else {
            System.out.println("KafkaConsumerUtil: 当前没有可用的消息。");
        }
        return message;
    }
}