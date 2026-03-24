package ru.yandex.practicum.telemetry.aggregator.service;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.aggregator.kafka.KafkaClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AggregatorServiceImpl implements AggregatorService {
    private final KafkaClient kafkaClient;

    public AggregatorServiceImpl(KafkaClient kafkaClient) {
        this.kafkaClient = kafkaClient;
    }
    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    @Value("${telemetry.aggregator.kafka.consumer.topic.snapshots}")
    private String snapshotsTopic;

    @Override
    public void aggregateSensorEvent(SensorEventAvro sensorEvent) {
        Optional<SensorsSnapshotAvro> snapshot = this.updateState(sensorEvent);
        if (snapshot.isPresent()) {
            SensorsSnapshotAvro snap = snapshot.get();
            kafkaClient.getProducer().send(new ProducerRecord<>(
                    snapshotsTopic,
                    null,
                    snap.getTimestamp().toEpochMilli(),
                    snap.getHubId(),
                    snap
            ));

        }
    }

    private Optional<SensorsSnapshotAvro> updateState(SensorEventAvro sensorEvent) {
        if (!snapshots.containsKey(sensorEvent.getHubId())) {
            snapshots.put(sensorEvent.getHubId(), SensorsSnapshotAvro.newBuilder()
                    .setHubId(sensorEvent.getHubId())
                    .setTimestamp(sensorEvent.getTimestamp())
                    .setSensorsState(new HashMap<>())
                    .build());
        }

        SensorsSnapshotAvro snapshot = snapshots.get(sensorEvent.getHubId());
        if (snapshot.getSensorsState().containsKey(sensorEvent.getId())) {
            SensorStateAvro lastEvent = snapshot.getSensorsState().get(sensorEvent.getId());
            if (lastEvent.getTimestamp().isAfter(sensorEvent.getTimestamp())
                    || lastEvent.getData().equals(sensorEvent.getPayload())) {
                return Optional.empty();
            }
        }

        SensorStateAvro updatedState = SensorStateAvro.newBuilder()
                .setData(sensorEvent.getPayload())
                .setTimestamp(sensorEvent.getTimestamp())
                .build();

        snapshot.getSensorsState().put(sensorEvent.getId(), updatedState);
        snapshot.setTimestamp(updatedState.getTimestamp());

        return Optional.of(snapshot);
    }
}
