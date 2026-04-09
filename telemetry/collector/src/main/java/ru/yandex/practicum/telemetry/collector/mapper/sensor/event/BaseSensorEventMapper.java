package ru.yandex.practicum.telemetry.collector.mapper.sensor.event;

import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Instant;

public abstract class BaseSensorEventMapper<T extends SpecificRecordBase> implements SensorEventMapper {
    @Override
    public SensorEventAvro mapToSensorEventAvro(SensorEventProto sensorEvent) {
        T payload = this.mapToPayloadAvro(sensorEvent);

        return SensorEventAvro.newBuilder()
                .setId(sensorEvent.getId())
                .setHubId(sensorEvent.getHubId())
                .setPayload(payload)
                .setTimestamp(Instant.ofEpochSecond(
                        sensorEvent.getTimestamp().getSeconds(),
                        sensorEvent.getTimestamp().getNanos()))
                .build();
    }

    protected abstract T mapToPayloadAvro(SensorEventProto sensorEvent);
}
