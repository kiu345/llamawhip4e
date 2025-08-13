package com.github.kiu345.eclipse.eclipseai.services.fixes;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kiu345.eclipse.eclipseai.adapter.ModelDescriptor;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.response.ChatResponse;

public class OllamaQwenFixer implements ChatFixer {
    private static class JSONCallQwen {
        @JsonProperty("name")
        public String name;
        @JsonProperty("arguments")
        public Map<String, String> arguments;
    }

    @Override
    public ChatResponse process(ModelDescriptor modelInfo, ChatResponse response, String data) throws Exception {
        if (modelInfo.model().startsWith("qwen2.5-coder") && data.startsWith("{")) {
            ObjectMapper mapper = new ObjectMapper();
            JSONCallQwen jsonCallQwen = mapper.readValue(data, JSONCallQwen.class);
            AiMessage msg = AiMessage.builder()
                    .toolExecutionRequests(
                            List.of(
                                    ToolExecutionRequest.builder()
                                            .name(jsonCallQwen.name)
                                            .arguments(mapper.writeValueAsString(jsonCallQwen.arguments))
                                            .build()
                            )
                    )
                    .text(response.aiMessage().text())
                    .build();

            return ChatResponse.builder()
                    .aiMessage(msg)
                    .id(response.id())
                    .metadata(response.metadata())
                    .build();
        }
        return response;
    }
}
