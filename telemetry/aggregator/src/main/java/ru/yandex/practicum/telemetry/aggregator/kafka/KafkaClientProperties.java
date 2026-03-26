package ru.yandex.practicum.telemetry.aggregator.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "telemetry.aggregator.kafka")
public class KafkaClientProperties {
    private String bootstrapServers;
    private ConsumerProperties consumer = new ConsumerProperties();

    @Getter
    @Setter
    public static class ConsumerProperties {
        private String groupId;
        private TopicProperties topic = new TopicProperties();
    }

    @Getter
    @Setter
    public static class TopicProperties {
        private String sensors;
        private String snapshots;
    }
}
