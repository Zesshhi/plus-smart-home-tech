package ru.yandex.practicum.telemetry.aggregator;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.aggregator.kafka.KafkaClient;
import ru.yandex.practicum.telemetry.aggregator.kafka.KafkaClientProperties;
import ru.yandex.practicum.telemetry.aggregator.service.AggregatorService;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AggregationStarter {
    private final AggregatorService aggregatorService;
    private final KafkaClient kafkaClient;
    private final KafkaClientProperties kafkaProperties;

    public void start() {
        Consumer<String, SpecificRecordBase> consumer = kafkaClient.getConsumer();

        try {
            consumer.subscribe(List.of(kafkaProperties.getConsumer().getTopic().getSensors()));

            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(Duration.ofSeconds(5));
                if (records.isEmpty()) {
                    continue;
                }

                for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                    aggregatorService.aggregateSensorEvent((SensorEventAvro) record.value());
                }

                consumer.commitSync();
            }
        } catch (WakeupException ignore) {
        } finally {
            try {
                consumer.commitSync();
            } finally {
                consumer.close();
            }
        }
    }
}
