package com.github.kiu345.eclipse.eclipseai.services.fixes;

import com.github.kiu345.eclipse.eclipseai.adapter.ModelDescriptor;

import dev.langchain4j.model.chat.response.ChatResponse;

public interface ChatFixer {
    default ChatResponse process(ModelDescriptor modelInfo, ChatResponse input, String data) throws Exception {
        return input;
    }
}
