package ru.yandex.practicum.telemetry.deserializer;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

public class SensorsSnapshotDeserializer implements Deserializer<SpecificRecordBase> {
    private final SpecificDatumReader<SensorsSnapshotAvro> reader =
            new SpecificDatumReader<>(SensorsSnapshotAvro.getClassSchema());
    private BinaryDecoder decoder;

    @Override
    public SpecificRecordBase deserialize(String topic, byte[] data) {
        try {
            if (data != null) {
                decoder = DecoderFactory.get().binaryDecoder(data, decoder);
                return reader.read(null, decoder);
            }
            return null;
        } catch (Exception ex) {
            throw new SerializationException("Deserialization error for topic [" + topic + "]", ex);
        }
    }
}
