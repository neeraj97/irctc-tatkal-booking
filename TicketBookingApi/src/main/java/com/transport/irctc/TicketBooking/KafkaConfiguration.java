package com.transport.irctc.TicketBooking;

import com.transport.irctc.TicketBooking.Events.TatkalEvent;
import com.transport.irctc.TicketBooking.Serializers.TatkalEventSerializer;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfiguration {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topic.tatkalEvents.name}")
    private String tatkalEventsTopic;


    @Value("${kafka.topic.tatkalTicketBookingStatus.name}")
    private String tatkalTicketBookingStatusTopic;

    @Value("${kafka.topic.partitions}")
    private int partitions;

    @Value("${kafka.topic.replicas}")
    private int replicas;

    @Value("${spring.kafka.client-id}")
    private String clientId;

    // tatkal events topic
    @Bean
    public NewTopic tatkalEventsTopic() {
        return TopicBuilder.name(tatkalEventsTopic)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    // topic for ticket booking status used by stream processors
    @Bean
    public NewTopic ticketBookingStatusTopic() {
        return TopicBuilder.name(tatkalTicketBookingStatusTopic)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    @Bean
    public ProducerFactory<Integer, TatkalEvent> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, TatkalEventSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        // adding one second of latency to wait
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1000);
        // adding 1 MB to be max batch_size
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 1024 * 1024);
        // making producer idempotent
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
//        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        // See https://kafka.apache.org/documentation/#producerconfigs for more properties
        return props;
    }

    @Bean
    public KafkaTemplate<Integer, TatkalEvent> kafkaTemplate() {
        KafkaTemplate<Integer,TatkalEvent> kafkaTemplate= new KafkaTemplate<Integer, TatkalEvent>(producerFactory());
        kafkaTemplate.setDefaultTopic(tatkalEventsTopic);
        return kafkaTemplate;
    }

}
