package ru.yandex.practicum.telemetry.collector.mapper.sensor.event;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorProto;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;

@Component
public class TemperatureSensorEventMapper extends BaseSensorEventMapper<TemperatureSensorAvro> {
    @Override
    public TemperatureSensorAvro mapToPayloadAvro(SensorEventProto sensorEvent) {
        TemperatureSensorProto proto = sensorEvent.getTemperatureSensor();
        return TemperatureSensorAvro.newBuilder()
                .setTemperatureC(proto.getTemperatureC())
                .setTemperatureF(proto.getTemperatureF())
                .build();
    }

    @Override
    public SensorEventProto.PayloadCase getEventType() {
        return SensorEventProto.PayloadCase.TEMPERATURE_SENSOR;
    }
}
