package ru.yandex.practicum.telemetry.collector.mapper.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.collector.mapper.hub.event.HubEventMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class HubEventsMapper {
    private final Map<HubEventProto.PayloadCase, HubEventMapper> hubEventMappers = new HashMap<>();

    HubEventsMapper(List<HubEventMapper> mapperBeans) {
        for (HubEventMapper hubEventMapper : mapperBeans) {
            hubEventMappers.put(hubEventMapper.getEventType(), hubEventMapper);
        }
    }

    public HubEventAvro mapToHubEventAvro(HubEventProto event) {
        if (hubEventMappers.containsKey(event.getPayloadCase())) {
            return hubEventMappers.get(event.getPayloadCase()).mapToHubEventAvro(event);
        }

        throw new UnknownHubEventTypeException(event.getPayloadCase().name());
    }
}
