package edu.fudan.se.sctap_lowcode_tool.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.concurrent.Future;

@Component
public class KafkaProducerUtil {

    private final KafkaProducer<String, String> producer;
    private final String topic;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KafkaProducerUtil(@Value("${kafka.bootstrap.servers}") String bootstrapServers,
                             @Value("${kafka.topic.producer}") String topic) {
        this.topic = topic;

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);

        this.producer = new KafkaProducer<>(props);
    }

    public Future<RecordMetadata> sendMessage(String key, Object message) {
        try {
            System.out.println("准备发送消息，key: " + key);
            System.out.println("消息内容：" + message);
            String jsonMessage = objectMapper.writeValueAsString(message);
            System.out.println("序列化后的JSON：" + jsonMessage);

            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, jsonMessage);
            System.out.println("准备发送到Kafka topic：" + topic);
            return producer.send(record);
        } catch (Exception e) {
            e.printStackTrace();  // 打印具体异常堆栈
            throw new RuntimeException("发送 Kafka 消息失败: " + e.getMessage(), e);
        }
    }

    public void close() {
        producer.close();
    }
}