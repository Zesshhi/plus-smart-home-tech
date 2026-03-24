package ru.yandex.practicum.telemetry.analyzer.service;

import com.google.protobuf.Timestamp;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc.HubRouterControllerBlockingStub;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.model.*;
import ru.yandex.practicum.telemetry.analyzer.repository.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class AnalyzerServiceImpl implements AnalyzerService {

    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;

    @GrpcClient("hub-router")
    private HubRouterControllerBlockingStub hubRouterClient;

    @Autowired
    public AnalyzerServiceImpl(ScenarioRepository scenarioRepository,
                                SensorRepository sensorRepository,
                                ConditionRepository conditionRepository,
                                ActionRepository actionRepository) {
        this.scenarioRepository = scenarioRepository;
        this.sensorRepository = sensorRepository;
        this.conditionRepository = conditionRepository;
        this.actionRepository = actionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public void analyzeSnapshot(SensorsSnapshotAvro snapshot) {
        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();

        scenarioRepository.findByHubId(snapshot.getHubId()).stream()
                .filter(scenario -> allConditionsMet(scenario.getConditions(), sensorsState))
                .flatMap(scenario -> executeScenario(scenario))
                .forEach(request -> hubRouterClient.handleDeviceAction(request));
    }

    @Override
    @Transactional
    public void handleHubEvent(HubEventAvro event) {
        Object payload = event.getPayload();
        if (payload instanceof DeviceAddedEventAvro added) {
            handleDeviceAdded(event.getHubId(), added);
        } else if (payload instanceof DeviceRemovedEventAvro removed) {
            handleDeviceRemoved(removed);
        } else if (payload instanceof ScenarioAddedEventAvro added) {
            handleScenarioAdded(event.getHubId(), added);
        } else if (payload instanceof ScenarioRemovedEventAvro removed) {
            handleScenarioRemoved(event.getHubId(), removed);
        }
    }

    private void handleDeviceAdded(String hubId, DeviceAddedEventAvro event) {
        if (sensorRepository.findByIdAndHubId(event.getId(), hubId).isPresent()) {
            return;
        }
        Sensor sensor = new Sensor();
        sensor.setId(event.getId());
        sensor.setHubId(hubId);
        sensorRepository.save(sensor);
    }

    private void handleDeviceRemoved(DeviceRemovedEventAvro event) {
        sensorRepository.deleteById(event.getId());
    }

    private void handleScenarioAdded(String hubId, ScenarioAddedEventAvro event) {
        Scenario scenario = scenarioRepository.findByHubIdAndName(hubId, event.getName())
                .orElseGet(Scenario::new);
        scenario.setHubId(hubId);
        scenario.setName(event.getName());
        scenario.getConditions().clear();
        scenario.getActions().clear();
        scenarioRepository.save(scenario);

        event.getConditions().forEach(c -> {
            Optional<Sensor> sensor = sensorRepository.findByIdAndHubId(c.getSensorId(), hubId);
            sensor.ifPresent(s -> {
                Condition condition = new Condition();
                condition.setType(ConditionType.valueOf(c.getType().name()));
                condition.setOperation(ConditionOperation.valueOf(c.getOperation().name()));
                condition.setValue(extractConditionValue(c));
                condition = conditionRepository.save(condition);

                ScenarioCondition sc = new ScenarioCondition();
                sc.setScenario(scenario);
                sc.setSensor(s);
                sc.setCondition(condition);
                scenario.getConditions().add(sc);
            });
        });

        event.getActions().forEach(a -> {
            Optional<Sensor> sensor = sensorRepository.findByIdAndHubId(a.getSensorId(), hubId);
            sensor.ifPresent(s -> {
                Action action = new Action();
                action.setType(ActionType.valueOf(a.getType().name()));
                action.setValue(a.getValue() instanceof Integer v ? v : null);
                action = actionRepository.save(action);

                ScenarioAction sa = new ScenarioAction();
                sa.setScenario(scenario);
                sa.setSensor(s);
                sa.setAction(action);
                scenario.getActions().add(sa);
            });
        });

        scenarioRepository.save(scenario);
    }

    private void handleScenarioRemoved(String hubId, ScenarioRemovedEventAvro event) {
        scenarioRepository.findByHubIdAndName(hubId, event.getName())
                .ifPresent(scenarioRepository::delete);
    }

    private boolean allConditionsMet(List<ScenarioCondition> conditions,
                                     Map<String, SensorStateAvro> sensorsState) {
        return conditions.stream().allMatch(sc -> {
            SensorStateAvro state = sensorsState.get(sc.getSensor().getId());
            if (state == null) return false;
            Integer sensorValue = extractSensorValue(sc.getCondition().getType(), state.getData());
            if (sensorValue == null) return false;
            return checkOperation(sc.getCondition().getOperation(), sensorValue, sc.getCondition().getValue());
        });
    }

    private Stream<DeviceActionRequest> executeScenario(Scenario scenario) {
        Timestamp now = Timestamp.newBuilder()
                .setSeconds(Instant.now().getEpochSecond())
                .build();
        return scenario.getActions().stream()
                .map(sa -> {
                    DeviceActionProto.Builder actionBuilder = DeviceActionProto.newBuilder()
                            .setSensorId(sa.getSensor().getId())
                            .setType(ActionTypeProto.valueOf(sa.getAction().getType().name()));
                    if (sa.getAction().getValue() != null) {
                        actionBuilder.setValue(sa.getAction().getValue());
                    }
                    return DeviceActionRequest.newBuilder()
                            .setHubId(scenario.getHubId())
                            .setScenarioName(scenario.getName())
                            .setAction(actionBuilder.build())
                            .setTimestamp(now)
                            .build();
                });
    }

    private Integer extractSensorValue(ConditionType type, Object data) {
        return switch (type) {
            case MOTION -> data instanceof MotionSensorAvro m ? (m.getMotion() ? 1 : 0) : null;
            case LUMINOSITY -> data instanceof LightSensorAvro l ? l.getLuminosity() : null;
            case SWITCH -> data instanceof SwitchSensorAvro s ? (s.getState() ? 1 : 0) : null;
            case TEMPERATURE -> switch (data) {
                case TemperatureSensorAvro t -> t.getTemperatureC();
                case ClimateSensorAvro c -> c.getTemperatureC();
                default -> null;
            };
            case CO2LEVEL -> data instanceof ClimateSensorAvro c ? c.getCo2Level() : null;
            case HUMIDITY -> data instanceof ClimateSensorAvro c ? c.getHumidity() : null;
        };
    }

    private boolean checkOperation(ConditionOperation op, int sensorValue, int conditionValue) {
        return switch (op) {
            case EQUALS -> sensorValue == conditionValue;
            case GREATER_THAN -> sensorValue > conditionValue;
            case LOWER_THAN -> sensorValue < conditionValue;
        };
    }

    private Integer extractConditionValue(ScenarioConditionAvro condition) {
        Object value = condition.getValue();
        if (value instanceof Integer i) return i;
        if (value instanceof Boolean b) return b ? 1 : 0;
        return null;
    }
}
