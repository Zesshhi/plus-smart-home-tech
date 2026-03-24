package ru.yandex.practicum.telemetry.analyzer.service;

import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

public interface AnalyzerService {
    void analyzeSnapshot(SensorsSnapshotAvro snapshot);
    void handleHubEvent(HubEventAvro event);
}
