package ru.yandex.practicum.telemetry.collector.mapper.sensor.event;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.MotionSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;

@Component
public class MotionSensorEventMapper extends BaseSensorEventMapper<MotionSensorAvro> {
    @Override
    public MotionSensorAvro mapToPayloadAvro(SensorEventProto sensorEvent) {
        MotionSensorProto proto = sensorEvent.getMotionSensor();
        return MotionSensorAvro.newBuilder()
                .setMotion(proto.getMotion())
                .setLinkQuality(proto.getLinkQuality())
                .setVoltage(proto.getVoltage())
                .build();
    }

    @Override
    public SensorEventProto.PayloadCase getEventType() {
        return SensorEventProto.PayloadCase.MOTION_SENSOR;
    }
}
