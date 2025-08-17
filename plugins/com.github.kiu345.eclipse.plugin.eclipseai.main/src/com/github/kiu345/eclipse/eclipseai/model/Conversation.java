package com.github.kiu345.eclipse.eclipseai.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.eclipse.e4.core.di.annotations.Creatable;

@Creatable
public class Conversation {
    public List<ChatMessage> conversation = new LinkedList<>();

    public int size() {
        return conversation.size();
    }

    public void clear() {
        conversation.clear();
    }

    public synchronized void add(ChatMessage message) {
        conversation.add(message);
    }

    public List<ChatMessage> messages() {
        return conversation;
    }

    public Optional<ChatMessage> removeLastMessage() {
        ChatMessage removed = !conversation.isEmpty() ? conversation.remove(conversation.size() - 1) : null;
        return Optional.ofNullable(removed);

    }
}
