package ru.yandex.practicum.telemetry.collector.mapper.sensor;

public class UnknownSensorEventTypeException extends RuntimeException {
    public UnknownSensorEventTypeException(String payloadCase) {
        super("Неизвестный тип события сенсора: " + payloadCase);
    }
}
