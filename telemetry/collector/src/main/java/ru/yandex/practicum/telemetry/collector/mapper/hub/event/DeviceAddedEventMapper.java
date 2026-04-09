package ru.yandex.practicum.telemetry.collector.mapper.hub.event;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;

@Component
public class DeviceAddedEventMapper extends BaseHubEventMapper<DeviceAddedEventAvro> {
    @Override
    protected DeviceAddedEventAvro mapToPayloadAvro(HubEventProto event) {
        DeviceAddedEventProto proto = event.getDeviceAdded();

        return DeviceAddedEventAvro.newBuilder()
                .setId(proto.getId())
                .setType(DeviceTypeAvro.valueOf(proto.getType().name()))
                .build();
    }

    @Override
    public HubEventProto.PayloadCase getEventType() {
        return HubEventProto.PayloadCase.DEVICE_ADDED;
    }
}
