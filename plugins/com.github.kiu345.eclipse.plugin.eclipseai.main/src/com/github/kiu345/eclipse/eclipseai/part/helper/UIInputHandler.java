package com.github.kiu345.eclipse.eclipseai.part.helper;

import java.util.UUID;

import org.eclipse.swt.browser.Browser;

import com.github.kiu345.eclipse.eclipseai.model.ChatMessage;

public abstract class UIInputHandler {
    /**
     * Replaces newline characters with line break escape sequences in the given
     * string.
     *
     * @param html The input string containing newline characters.
     * @return A string with newline characters replaced by line break escape
     *         sequences.
     */
    public static String fixLineBreaks(String html) {
        return html.replace("\n", "\\n").replace("\r", "");
    }

    /**
     * Escapes HTML quotation marks in the given string.
     * 
     * @param html The input string containing HTML.
     * @return A string with escaped quotation marks for proper HTML handling.
     */
    public static String escapeHtmlQuotes(String html) {
        return html.replace("\"", "\\\"").replace("'", "\\'");
    }
    
    
    abstract public void createElement(Browser browser, UUID messageId, String role);
    abstract public void updateElement(Browser browser, UUID messageId, ChatMessage.Type type, String body);
    abstract public void closeElement(Browser browser, UUID messageId);
}
