package ru.yandex.practicum.telemetry.analyzer.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.analyzer.service.AnalyzerService;
import ru.yandex.practicum.telemetry.deserializer.HubEventDeserializer;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Slf4j
@Component
public class HubEventProcessor implements Runnable {

    private final AnalyzerService analyzerService;
    private final Consumer<String, SpecificRecordBase> consumer;

    @Value("${telemetry.analyzer.kafka.consumer.hub-events.topic}")
    private String hubEventsTopic;

    public HubEventProcessor(AnalyzerService analyzerService,
                              @Value("${telemetry.analyzer.kafka.bootstrap-servers}") String bootstrapServers,
                              @Value("${telemetry.analyzer.kafka.consumer.hub-events.group-id}") String groupId) {
        this.analyzerService = analyzerService;
        Properties config = new Properties();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, HubEventDeserializer.class);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        this.consumer = new KafkaConsumer<>(config);
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
    }

    @Override
    public void run() {
        try {
            consumer.subscribe(List.of(hubEventsTopic));
            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(Duration.ofSeconds(1));
                for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                    analyzerService.handleHubEvent((HubEventAvro) record.value());
                }
            }
        } catch (WakeupException ignore) {
        } catch (Exception e) {
            log.error("Error processing hub event", e);
        } finally {
            consumer.close();
        }
    }
}
