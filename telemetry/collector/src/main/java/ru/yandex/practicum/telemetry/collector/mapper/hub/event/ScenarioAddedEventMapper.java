package ru.yandex.practicum.telemetry.collector.mapper.hub.event;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioAddedEventProto;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.telemetry.collector.mapper.hub.event.scenario.DeviceActionMapper;
import ru.yandex.practicum.telemetry.collector.mapper.hub.event.scenario.ScenarioConditionMapper;

@Component
public class ScenarioAddedEventMapper extends BaseHubEventMapper<ScenarioAddedEventAvro> {
    @Override
    protected ScenarioAddedEventAvro mapToPayloadAvro(HubEventProto event) {
        ScenarioAddedEventProto proto = event.getScenarioAdded();

        return ScenarioAddedEventAvro.newBuilder()
                .setName(proto.getName())
                .setActions(proto.getActionList().stream()
                        .map(DeviceActionMapper::mapToAvro)
                        .toList())
                .setConditions(proto.getConditionList().stream()
                        .map(ScenarioConditionMapper::mapToAvro)
                        .toList())
                .build();
    }

    @Override
    public HubEventProto.PayloadCase getEventType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }
}
