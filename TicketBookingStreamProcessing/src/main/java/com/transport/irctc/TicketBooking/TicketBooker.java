package com.transport.irctc.TicketBooking;

import com.transport.irctc.TicketBooking.Events.TatkalEvent;
import com.transport.irctc.TicketBooking.Events.TicketBookingStatusEvent;
import com.transport.irctc.TicketBooking.Serdes.ObjectDeserializer;
import com.transport.irctc.TicketBooking.Serdes.ObjectSerializer;
import com.transport.irctc.TicketBooking.Serdes.TrainSeatInventorySerde;
import com.transport.irctc.TicketBooking.Serdes.TrainWaitlistSerde;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.Stores;

import java.util.Properties;
import java.util.ResourceBundle;

public class TicketBooker {

    private static ResourceBundle rb = ResourceBundle.getBundle("application");

    public final static String sourceTopic = rb.getString("kafka.streams.sourceTopic");
    public final static String sinkTopic = rb.getString("kafka.streams.sinkTopic");

    public static void main(String[] args) {

        Topology topology = buildTopology();
        System.out.println(topology.describe());

        KafkaStreams streams = new KafkaStreams(topology, getStreamApplicationProperties());
        // starting streams application
        streams.start();
        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> streams.close()));
    }

    public static Properties getStreamApplicationProperties() {
        Properties prop = new Properties();
        prop.put(StreamsConfig.APPLICATION_ID_CONFIG, rb.getString("kafka.client-id"));
        prop.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, rb.getString("kafka.bootstrap-servers"));
        prop.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);
        prop.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        //prop.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG,3000L);
        return prop;
    }

    public static Topology buildTopology() {
        Serde<TrainSeatInventory> trainSeatInventorySerde = new TrainSeatInventorySerde();
        Serde<TrainWaitlist> trainWaitlistSerde = new TrainWaitlistSerde();
        // building topology
        Topology topology = new Topology();
        topology.addSource("sourceProcessor", new IntegerDeserializer(),
                new ObjectDeserializer<TatkalEvent>(TatkalEvent.class), sourceTopic)
                .addProcessor("TicketProcessor", () -> new TicketProcessor(), "sourceProcessor")
                .addStateStore(
                        Stores.keyValueStoreBuilder(
                                Stores.inMemoryKeyValueStore(TicketProcessor.getTrainSeatInventoryStoreName()),
                                Serdes.Integer(),
                                trainSeatInventorySerde),
                        "TicketProcessor")
                .addStateStore(
                        Stores.keyValueStoreBuilder(
                                Stores.inMemoryKeyValueStore(TicketProcessor.getTrainWaitlistStoreName()),
                                Serdes.Integer(),
                                trainWaitlistSerde),
                        "TicketProcessor")
                .addSink("sinkProcessor", sinkTopic,
                        new IntegerSerializer(), new ObjectSerializer<TicketBookingStatusEvent>(), "TicketProcessor");
        return topology;
    }
}