package com.github.kiu345.eclipse.eclipseai.messaging;

import java.util.Objects;
import java.util.UUID;

public class AgentMsg extends MessageBase implements Msg {

    private String thinking;

    public AgentMsg() {
        super();
    }

    public AgentMsg(UUID messageId) {
        super(messageId);
    }

    public AgentMsg(String message) {
        super(message);
    }

    public AgentMsg(UUID messageId, String message) {
        super(messageId, message);
    }

    public AgentMsg(UUID messageId, String message, String thinking) {
        super(messageId, message);
        this.thinking = thinking;
    }

    public String getThinking() {
        return thinking;
    }

    public void setThinking(String thinking) {
        this.thinking = thinking;
    }

    @Override
    public Source getSource() {
        return Source.AGENT;
    }

    @Override
    public int hashCode() {
        final int prime = 89 ;
        int result = super.hashCode();
        result = prime * result + Objects.hash(thinking);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (obj instanceof AgentMsg other) {
            return Objects.equals(thinking, other.thinking);
        }
        return false;
    }

}
