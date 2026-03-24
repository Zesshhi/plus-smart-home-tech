package ru.yandex.practicum.telemetry.aggregator.kafka;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.telemetry.deserializer.SensorEventDeserializer;
import ru.yandex.practicum.telemetry.serializer.GeneralAvroSerializer;

import java.time.Duration;
import java.util.Properties;

@Component
public class KafkaClientImpl implements KafkaClient {
    private Producer<String, SpecificRecordBase> producer;
    private Consumer<String, SpecificRecordBase> consumer;

    @Value("${telemetry.aggregator.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${telemetry.aggregator.kafka.consumer.group-id}")
    private String consumerGroupId;

    public Producer<String, SpecificRecordBase> getProducer() {
        if (producer == null) {
            Properties config = new Properties();
            config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
            config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class);

            this.producer = new KafkaProducer<>(config);
        }

        return producer;
    }

    public Consumer<String, SpecificRecordBase> getConsumer() {
        if (consumer == null) {
            Properties config = new Properties();
            config.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
            config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
            config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorEventDeserializer.class);
            config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

            this.consumer = new KafkaConsumer<>(config);

            Runtime.getRuntime().addShutdownHook(new Thread(this.consumer::wakeup));
        }

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
