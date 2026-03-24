package ru.yandex.practicum.telemetry.collector.mapper.sensor.event;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ClimateSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;

@Component
public class ClimateSensorEventMapper extends BaseSensorEventMapper<ClimateSensorAvro> {
    @Override
    public ClimateSensorAvro mapToPayloadAvro(SensorEventProto event) {
        ClimateSensorProto proto = event.getClimateSensor();
        return ClimateSensorAvro.newBuilder()
                .setCo2Level(proto.getCo2Level())
                .setHumidity(proto.getHumidity())
                .setTemperatureC(proto.getTemperatureC())
                .build();
    }

    @Override
    public SensorEventProto.PayloadCase getEventType() {
        return SensorEventProto.PayloadCase.CLIMATE_SENSOR;
    }
}
