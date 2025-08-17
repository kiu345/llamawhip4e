package com.github.kiu345.eclipse.eclipseai.messaging;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "source"
)
@JsonSubTypes(
    {
            @JsonSubTypes.Type(value = SystemMsg.class, name = "SYSTEM"),
            @JsonSubTypes.Type(value = UserMsg.class, name = "USER"),
            @JsonSubTypes.Type(value = AgentMsg.class, name = "AGENT"),
            @JsonSubTypes.Type(value = ToolsMsg.class, name = "TOOL"),
    }
)
@JsonIgnoreProperties({ "type" })
public interface Msg {
    public static enum Type {
        MESSAGE,
        ERROR,
        SYSTEM,
        UNDEF
    }

    public static enum Source {
        SYSTEM,
        USER,
        AGENT,
        TOOL;
    }

    default Type getType() {
        return Type.MESSAGE;
    }

    Source getSource();

    UUID getMessageId();

    Double getTimings();

    Instant getTimestamp();

    String getMessage();
}
