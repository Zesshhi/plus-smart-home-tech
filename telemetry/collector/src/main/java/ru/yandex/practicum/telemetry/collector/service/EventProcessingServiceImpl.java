package ru.yandex.practicum.telemetry.collector.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.telemetry.collector.kafka.KafkaClient;
import ru.yandex.practicum.telemetry.collector.mapper.hub.HubEventsMapper;
import ru.yandex.practicum.telemetry.collector.mapper.sensor.SensorEventsMapper;

import java.time.Instant;

@Service
public class EventProcessingServiceImpl implements EventProcessingService {
    private final HubEventsMapper hubEventsMapper;
    private final SensorEventsMapper sensorEventsMapper;
    private final KafkaClient kafkaClient;

    public EventProcessingServiceImpl(HubEventsMapper hubEventsMapper,
                                      SensorEventsMapper sensorEventsMapper,
                                      KafkaClient kafkaClient) {
        this.hubEventsMapper = hubEventsMapper;
        this.sensorEventsMapper = sensorEventsMapper;
        this.kafkaClient = kafkaClient;
    }

    @Value("${telemetry.collector.kafka.producer.topic.hub-events}")
    private String hubEventsTopic;
    @Value("${telemetry.collector.kafka.producer.topic.sensor-events}")
    private String sensorEventsTopic;

    @Override
    public void processHubEvent(HubEventProto event) {
        kafkaClient.send(
                hubEventsTopic,
                hubEventsMapper.mapToHubEventAvro(event),
                Instant.ofEpochSecond(
                        event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()).toEpochMilli(),
                event.getHubId());
    }

    @Override
    public void processSensorEvent(SensorEventProto event) {
        kafkaClient.send(
                sensorEventsTopic,
                sensorEventsMapper.mapToSensorEventAvro(event),
                Instant.ofEpochSecond(
                        event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()).toEpochMilli(),
                event.getHubId());
    }
}
