package ru.yandex.practicum.telemetry.collector.mapper.sensor.event;

import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

public interface SensorEventMapper {
    SensorEventAvro mapToSensorEventAvro(SensorEventProto sensorEvent);

    SensorEventProto.PayloadCase getEventType();
}
