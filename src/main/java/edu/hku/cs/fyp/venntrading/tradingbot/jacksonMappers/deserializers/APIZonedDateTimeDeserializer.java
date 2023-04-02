package edu.hku.cs.fyp.venntrading.tradingbot.jacksonMappers.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class APIZonedDateTimeDeserializer extends StdDeserializer<ZonedDateTime> {
    public APIZonedDateTimeDeserializer() {
        this(null);
    }

    public APIZonedDateTimeDeserializer(Class<ZonedDateTime> t) {
        super(t);
    }

    @Override
    public ZonedDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode jsonNode = parser.getCodec().readTree(parser);
        long millis = jsonNode.get(6).asLong();
        Instant instant = Instant.ofEpochMilli(millis);
        return instant.atZone(ZoneId.of("Asia/Hong_Kong"));
    }
}