package ru.yandex.practicum.telemetry.collector.mapper.hub;

public class UnknownHubEventTypeException extends RuntimeException {
    public UnknownHubEventTypeException(String payloadCase) {
        super("Неизвестный тип события хаба: " + payloadCase);
    }
}
