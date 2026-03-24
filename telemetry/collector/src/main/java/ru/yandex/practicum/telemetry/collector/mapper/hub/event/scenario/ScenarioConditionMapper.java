package ru.yandex.practicum.telemetry.collector.mapper.hub.event.scenario;

import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;

public class ScenarioConditionMapper {
    public static ScenarioConditionAvro mapToAvro(ScenarioConditionProto data) {
        ScenarioConditionAvro.Builder builder = ScenarioConditionAvro.newBuilder()
                .setType(ConditionTypeAvro.valueOf(data.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(data.getOperation().name()))
                .setSensorId(data.getSensorId());

        switch (data.getValueCase()) {
            case ScenarioConditionProto.ValueCase.BOOL_VALUE -> builder.setValue(data.getBoolValue());
            case ScenarioConditionProto.ValueCase.INT_VALUE -> builder.setValue(data.getIntValue());
        }

        return builder.build();
    }
}
