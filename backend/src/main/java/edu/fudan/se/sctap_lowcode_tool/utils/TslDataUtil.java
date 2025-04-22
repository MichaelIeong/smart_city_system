//package edu.fudan.se.sctap_lowcode_tool.utils;
//
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.apache.kafka.clients.consumer.ConsumerRecords;
//import org.apache.kafka.clients.consumer.KafkaConsumer;
//import org.springframework.stereotype.Component;
//
//import java.time.Duration;
//import java.util.Collections;
//import java.util.Properties;
//
///**
// * 用于获取特斯联的数据的kafka消费者工具类
// */
//@Component
//public class TslDataUtil {
//
//    private final KafkaConsumer<String, String> consumer;
//
//    public TslDataUtil() {
//        // 配置消费者
//        Properties props = new Properties();
//        props.put("bootstrap.servers", "10.37.71.12:19092"); // Kafka 订阅地址
//        props.put("group.id", "sc_uos_consumer_group");     // 消费者组 ID
//        props.put("enable.auto.commit", "true");            // 自动提交偏移量
//        props.put("auto.commit.interval.ms", "1000");       // 自动提交间隔
//        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer"); // 反序列化键
//        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer"); // 反序列化值
//        props.put("auto.offset.reset", "earliest");         // 从最早的消息开始消费
//
//        // 创建消费者
//        this.consumer = new KafkaConsumer<>(props);
//
//        // 订阅指定的 Topic
//        this.consumer.subscribe(Collections.singletonList("SC-UOS-Platform"));
//    }
//
//    /**
//     * 消费消息的方法
//     */
//    public void consumeMessages() {
//        System.out.println("开始订阅 SC-UOS-Platform 主题的消息...");
//
//        try {
//            // 持续消费
//            while (true) {
//                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000)); // 每秒轮询
//                for (ConsumerRecord<String, String> record : records) {
//                    // 打印消息内容
//                    System.out.printf("Offset: %d, Key: %s, Value: %s, Partition: %d%n",
//                            record.offset(), record.key(), record.value(), record.partition());
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("Kafka 消息消费发生异常：" + e.getMessage());
//        } finally {
//            consumer.close();
//        }
//    }
//}