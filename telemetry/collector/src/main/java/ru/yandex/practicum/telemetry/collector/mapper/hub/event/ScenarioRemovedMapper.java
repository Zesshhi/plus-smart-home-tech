package ru.yandex.practicum.telemetry.collector.mapper.hub.event;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioRemovedEventProto;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

@Component
public class ScenarioRemovedMapper extends BaseHubEventMapper<ScenarioRemovedEventAvro> {

    @Override
    protected ScenarioRemovedEventAvro mapToPayloadAvro(HubEventProto event) {
        ScenarioRemovedEventProto proto = event.getScenarioRemoved();

        return ScenarioRemovedEventAvro.newBuilder()
                .setName(proto.getName())
                .build();
    }

    @Override
    public HubEventProto.PayloadCase getEventType() {
        return HubEventProto.PayloadCase.SCENARIO_REMOVED;
    }
}
