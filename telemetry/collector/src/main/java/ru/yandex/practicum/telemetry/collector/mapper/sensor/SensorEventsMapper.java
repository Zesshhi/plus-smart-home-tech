package ru.yandex.practicum.telemetry.collector.mapper.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.mapper.sensor.event.SensorEventMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SensorEventsMapper {
    public final Map<SensorEventProto.PayloadCase, SensorEventMapper> sensorEventMappers = new HashMap<>();

    SensorEventsMapper(List<SensorEventMapper> mapperBeans) {
        for (SensorEventMapper sensorEventMapper : mapperBeans) {
            sensorEventMappers.put(sensorEventMapper.getEventType(), sensorEventMapper);
        }
    }

    public SensorEventAvro mapToSensorEventAvro(SensorEventProto sensorEvent) {
        if (sensorEventMappers.containsKey(sensorEvent.getPayloadCase())) {
            return sensorEventMappers.get(sensorEvent.getPayloadCase()).mapToSensorEventAvro(sensorEvent);
        }

        throw new RuntimeException("Unknown sensor event type:  " + sensorEvent.getPayloadCase());
    }
}
