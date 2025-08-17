package com.github.kiu345.eclipse.eclipseai.services.tools;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import org.eclipse.core.runtime.ILog;

import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Inject;

public class SimpleAITools {
    @SuppressWarnings("unused")
    @Inject
    private ILog log;

    @Tool("Returns the current local date and time")
    public String dateAndTime() {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(LocalDateTime.now());
    }
}
