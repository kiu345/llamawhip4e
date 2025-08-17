package com.github.kiu345.eclipse.eclipseai.messaging;

import java.util.UUID;

public class SystemMsg extends MessageBase implements Msg {

    public SystemMsg() {
        super();
    }

    public SystemMsg(UUID messageId) {
        super(messageId);
    }

    public SystemMsg(UUID messageId, String message) {
        super(messageId, message);
    }

    public SystemMsg(String message) {
        super(message);
    }

    @Override
    public Source getSource() {
        return Source.SYSTEM;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof SystemMsg)) {
            return false;
        }
        return true;
    }

}
