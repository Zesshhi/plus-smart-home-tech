package ru.yandex.practicum.telemetry.collector.mapper.hub.event;

import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

public interface HubEventMapper {
    HubEventAvro mapToHubEventAvro(HubEventProto event);

    HubEventProto.PayloadCase getEventType();
}
