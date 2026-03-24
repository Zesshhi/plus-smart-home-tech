package ru.yandex.practicum.telemetry.collector.mapper.hub.event;

import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Instant;

public abstract class BaseHubEventMapper<T extends SpecificRecordBase> implements HubEventMapper {
    @Override
    public HubEventAvro mapToHubEventAvro(HubEventProto event) {
        T payload = this.mapToPayloadAvro(event);

        return HubEventAvro.newBuilder()
                .setPayload(payload)
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(
                        event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()))
                .build();
    }

    protected abstract T mapToPayloadAvro(HubEventProto eventDto);
}
