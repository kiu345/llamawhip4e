package com.github.kiu345.eclipse.eclipseai.messaging;

import java.util.Objects;
import java.util.UUID;

import dev.langchain4j.data.message.ToolExecutionResultMessage;

public class ToolsMsg extends MessageBase implements Msg {

    private String requestId;
    private String toolName;

    public ToolsMsg() {
        super();
    }

    public ToolsMsg(UUID messageId) {
        super(messageId);
    }

    public ToolsMsg(UUID messageId, String requestId, String toolName, String value) {
        super(messageId, value);
        this.requestId = requestId;
        this.toolName = toolName;
    }

    public ToolsMsg(ToolExecutionResultMessage e) {
        this(UUID.randomUUID(), e.id(), e.toolName(), e.text());
    }

    @Override
    public Source getSource() {
        return Source.TOOL;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    @Override
    public int hashCode() {
        final int prime = 13;
        int result = super.hashCode();
        result = prime * result + Objects.hash(super.hashCode(), requestId, toolName);
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
        if (!(obj instanceof ToolsMsg)) {
            return false;
        }
        ToolsMsg other = (ToolsMsg) obj;
        return super.equals(obj) && Objects.equals(requestId, other.requestId) && Objects.equals(toolName, other.toolName);
    }

}
