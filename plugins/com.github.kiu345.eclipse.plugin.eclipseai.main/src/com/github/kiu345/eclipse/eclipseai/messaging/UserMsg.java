package com.github.kiu345.eclipse.eclipseai.messaging;

import java.util.Objects;
import java.util.UUID;

public class UserMsg extends MessageBase implements Msg {

    private boolean predefinedPrompt = false;

    public UserMsg() {
        super();
    }

    public UserMsg(UUID messageId) {
        super(messageId);
    }

    public UserMsg(String message) {
        super(message);
    }

    public UserMsg(UUID messageId, String message) {
        super(messageId, message);
    }

    @Override
    public Source getSource() {
        return Source.USER;
    }

    public boolean isPredefinedPrompt() {
        return predefinedPrompt;
    }

    public void setPredefinedPrompt(boolean predefinedPrompt) {
        this.predefinedPrompt = predefinedPrompt;
    }

    @Override
    public int hashCode() {
        final int prime = 67;
        int result = super.hashCode();
        result = prime * result + Objects.hash(predefinedPrompt);
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
        if (obj instanceof UserMsg other) {
            return predefinedPrompt == other.predefinedPrompt;
        }
        return false;
    }
}
