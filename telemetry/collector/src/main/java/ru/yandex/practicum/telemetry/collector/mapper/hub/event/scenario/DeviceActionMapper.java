package ru.yandex.practicum.telemetry.collector.mapper.hub.event.scenario;

import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;

public class DeviceActionMapper {
    public static DeviceActionAvro mapToAvro(DeviceActionProto data) {
        return DeviceActionAvro.newBuilder()
                .setType(ActionTypeAvro.valueOf(data.getType().name()))
                .setSensorId(data.getSensorId())
                .setValue(data.getValue())
                .build();
    }
}
