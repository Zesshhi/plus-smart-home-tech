package ru.yandex.practicum.telemetry.collector.service;

import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

public interface EventProcessingService {
    void processSensorEvent(SensorEventProto event);

    void processHubEvent(HubEventProto event);
}
