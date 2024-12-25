package edu.fudan.se.sctap_lowcode_tool.utils;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class KafkaConsumerUtil implements Runnable {

    private final KafkaConsumer<String, String> consumer;
    /**
     * 用于标识当前消费者是否在运行
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    public KafkaConsumerUtil() {
        // 1. 配置消费者
        Properties props = new Properties();
        props.put("bootstrap.servers", "10.37.71.12:19092"); // Kafka 地址
        props.put("group.id", "sc_uos_consumer_group");     // 消费者组 ID
        props.put("enable.auto.commit", "true");            // 自动提交偏移量
        props.put("auto.commit.interval.ms", "1000");       // 自动提交间隔
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");   // 反序列化键
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer"); // 反序列化值
        props.put("auto.offset.reset", "earliest");         // 从最早的消息开始消费

        // 2. 创建消费者
        this.consumer = new KafkaConsumer<>(props);

        // 3. 订阅指定的 Topic
        this.consumer.subscribe(Collections.singletonList("SC-UOS-Platform"));

        System.out.println("KafkaConsumerService: 初始化完成，订阅了 Topic: SC-UOS-Platform");
    }

    /**
     * 作为 Runnable 的 run 方法。
     * 当运行此方法时，会一直持续消费，直到调用 close()。
     */
    @Override
    public void run() {
        running.set(true);
        try {
            System.out.println("KafkaConsumerService: 开始消费消息...");
            while (running.get()) {
                // 每秒轮询一次
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    // 这里进行业务处理，如打印日志、写数据库等
                    System.out.printf("Offset: %d, Key: %s, Value: %s, Partition: %d%n",
                            record.offset(), record.key(), record.value(), record.partition());
                }
            }
        } catch (WakeupException e) {
            // 如果是我们主动调用 consumer.wakeup()，则 running 会被置为 false
            if (running.get()) {
                // 如果依然是 true，说明不是主动关闭引起的 wakeup，继续抛出异常
                throw e;
            }
            // 否则说明是我们主动停止消费了，这里可以捕获后直接结束
        } catch (Exception e) {
            System.err.println("KafkaConsumerService: 消费过程中发生异常 -> " + e.getMessage());
        } finally {
            // 只有在外部调用 close() 后，才会到达这里
            consumer.close();
            System.out.println("KafkaConsumerService: Consumer 已关闭");
        }
    }

    /**
     * 安全停止消费并关闭 Consumer
     */
    public void close() {
        System.out.println("KafkaConsumerService: 收到关闭请求...");
        running.set(false);
        consumer.wakeup(); // 会触发 WakeupException，从而跳出消费循环
    }
}