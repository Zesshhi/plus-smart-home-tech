package ru.yandex.practicum.telemetry.aggregator.kafka;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.telemetry.deserializer.SensorEventDeserializer;
import ru.yandex.practicum.telemetry.serializer.GeneralAvroSerializer;

import java.time.Duration;
import java.util.Properties;

@Component
@RequiredArgsConstructor
public class KafkaClientImpl implements KafkaClient {
    private final KafkaClientProperties kafkaProperties;

    private Producer<String, SpecificRecordBase> producer;
    private Consumer<String, SpecificRecordBase> consumer;

    @PostConstruct
    public void init() {
        Properties producerConfig = new Properties();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class);
        this.producer = new KafkaProducer<>(producerConfig);

        Properties consumerConfig = new Properties();
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getConsumer().getGroupId());
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorEventDeserializer.class);
        consumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        this.consumer = new KafkaConsumer<>(consumerConfig);

        Runtime.getRuntime().addShutdownHook(new Thread(this.consumer::wakeup));
    }

    public Producer<String, SpecificRecordBase> getProducer() {
        return producer;
    }

    public Consumer<String, SpecificRecordBase> getConsumer() {
        return consumer;
    }

    @Override
    public void close() {
        if (producer != null) {
            producer.flush();
            producer.close(Duration.ofSeconds(5));
        }
    }
}
