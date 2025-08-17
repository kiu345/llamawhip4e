package com.github.kiu345.eclipse.eclipseai.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.github.kiu345.eclipse.eclipseai.part.Attachment;

/**
 * Represents a chat message with an ID, role, number of tokens, and the message
 * content.
 */
public class ChatMessage {
    
    public static enum Type {
        MESSAGE,
        ERROR,
        SYSTEM
    }
    
    public static final String ROLE_SYSTEM = "system";
    public static final String ROLE_AI = "assistant";
    public static final String ROLE_USER = "user";
    
    public final UUID id;

    public final String role;

    public final String name;

    public StringBuffer content;

    private final List<Attachment> attachments;

    /**
     * Constructs a ChatMessage with the given ID and role.
     * 
     * @param id
     *             The unique identifier for the chat message
     * @param role
     *             The role associated with the chat message (e.g., "user",
     *             "assistant")
     */
    public ChatMessage(UUID id, String role) {
        this(id, null, role);
    }

    public ChatMessage(UUID id, String name, String role) {
        this.id = id;
        this.role = role;
        this.name = name;
        this.content = new StringBuffer();
        this.attachments = new ArrayList<>();
    }

    /**
     * Appends the given message to the existing message.
     * 
     * @param msg
     *            The message to be appended
     */
    public void append(String msg) {
        this.content.append(msg);
    }

    /**
     * Retrieves the message content.
     * 
     * @return The message content
     */
    public String getContent() {
        return content.toString();
    }

    /**
     * Sets the message content.
     * 
     * @param message
     *                The new message content
     */
    public void setContent(String message) {
        this.content.setLength(0);
        this.content.append(message);
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments.clear();
        this.attachments.addAll(attachments);
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    /**
     * Retrieves the unique identifier.
     * 
     * @return The ID of the chat message
     */
    public UUID getId() {
        return id;
    }

    /**
     * Retrieves the role associated with the chat message.
     * 
     * @return The role of the chat message
     */
    public String getRole() {
        return role;
    }

    public String getName() {
        return name;
    }

}
