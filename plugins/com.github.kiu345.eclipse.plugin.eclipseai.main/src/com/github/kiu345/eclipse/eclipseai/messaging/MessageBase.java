package com.github.kiu345.eclipse.eclipseai.messaging;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.github.kiu345.eclipse.eclipseai.messaging.Msg.Source;

public abstract class MessageBase {
    private UUID messageId = UUID.randomUUID();
    private String message;
    private Double timings;

    /**
     * Only seconds to precision, otherwise we get serialize errors
     */
    private Instant timestamp = Instant.now();

    protected MessageBase() {
    }

    protected MessageBase(UUID messageId) {
        this.messageId = Objects.requireNonNull(messageId);
    }

    protected MessageBase(String message) {
        this.message = message;
    }

    protected MessageBase(UUID messageId, String message) {
        this.messageId = messageId;
        this.message = message;
    }

    public UUID getMessageId() {
        return messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = Objects.requireNonNull(messageId);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Double getTimings() {
        return timings;
    }

    public void setTimings(Double timings) {
        this.timings = timings;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "%s[%s:%s] %s (%s) %f".formatted(hashCode(), getSource(), messageId, getMessage(), timestamp.toString(), timings);
    }

    abstract public Source getSource();

    @Override
    public int hashCode() {
        return Objects.hash(message, messageId, timestamp, timings);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        MessageBase other = (MessageBase) obj;
        return Objects.equals(message, other.message) && Objects.equals(messageId, other.messageId)
                && Objects.equals(timestamp, other.timestamp)
                && Objects.equals(timings, other.timings);
    }

}
