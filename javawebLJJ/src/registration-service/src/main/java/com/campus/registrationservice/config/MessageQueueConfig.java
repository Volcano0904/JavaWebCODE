package com.campus.registrationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * 消息队列配置类
 */
@Configuration
@EnableKafka
public class MessageQueueConfig {

    // 配置Kafka监听器工厂
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    // 配置消费者工厂
    @Bean
    public org.springframework.kafka.core.ConsumerFactory<String, Object> consumerFactory() {
        // 这里配置消费者工厂
        return new org.springframework.kafka.core.DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    // 消费者配置
    @Bean
    public java.util.Map<String, Object> consumerConfigs() {
        java.util.Map<String, Object> props = new java.util.HashMap<>();
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, 
                  "localhost:9092");
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG, 
                  "registration-group");
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, 
                  org.apache.kafka.common.serialization.StringDeserializer.class);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, 
                  org.springframework.kafka.support.serializer.JsonDeserializer.class);
        props.put(org.springframework.kafka.support.serializer.JsonDeserializer.VALUE_DEFAULT_TYPE, 
                  "java.lang.Object");
        return props;
    }

    // 配置Kafka模板
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>();
        template.setProducerFactory(producerFactory());
        return template;
    }

    // 配置生产者工厂
    @Bean
    public org.springframework.kafka.core.ProducerFactory<String, Object> producerFactory() {
        return new org.springframework.kafka.core.DefaultKafkaProducerFactory<>(producerConfigs());
    }

    // 生产者配置
    @Bean
    public java.util.Map<String, Object> producerConfigs() {
        java.util.Map<String, Object> props = new java.util.HashMap<>();
        props.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, 
                  "localhost:9092");
        props.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, 
                  org.apache.kafka.common.serialization.StringSerializer.class);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, 
                  org.springframework.kafka.support.serializer.JsonSerializer.class);
        return props;
    }
}