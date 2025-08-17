package com.github.kiu345.eclipse.eclipseai.util.json;

import java.io.IOException;
import java.time.Instant;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class StringToInstantDeserializer extends JsonDeserializer<Instant> {
    @Override
    public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        var dateTime = InstantToStringSerializer.FORMATTER.parse(p.getText());
        return Instant.from(dateTime);
    }
}