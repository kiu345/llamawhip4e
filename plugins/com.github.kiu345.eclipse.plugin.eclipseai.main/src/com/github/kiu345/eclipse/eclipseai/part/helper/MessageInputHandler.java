package com.github.kiu345.eclipse.eclipseai.part.helper;

import java.util.UUID;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.swt.browser.Browser;

import com.github.kiu345.eclipse.eclipseai.model.ChatMessage;
import com.github.kiu345.eclipse.eclipseai.prompt.MessageParser;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Creatable
@Singleton
public class MessageInputHandler extends UIInputHandler {
    @Inject
    private UISynchronize uiSync;

    @Override
    public void createElement(Browser browser, UUID messageId, String role) {
        String cssClass = "user".equals(role) ? "chat-bubble me" : "chat-bubble you";
        uiSync.asyncExec(() -> {
            browser.execute("""
                    node = document.createElement("div");
                    node.setAttribute("id", "message-${id}");
                    node.setAttribute("class", "${cssClass}");
                    document.getElementById("content").appendChild(node);
                        """.replace("${id}", messageId.toString()).replace("${cssClass}", cssClass));
            browser.execute(
                    // Scroll down
                    "window.scrollTo(0, document.body.scrollHeight);"
            );
        });
    }

    @Override
    public void updateElement(Browser browser, UUID messageId, ChatMessage.Type type, String body) {
        uiSync.asyncExec(() -> {
            MessageParser parser = new MessageParser();
            String fixedHtml = escapeHtmlQuotes(fixLineBreaks(parser.parseToHtml(messageId, body)));
            switch (type) {
                case ERROR:
                    fixedHtml = "<div style=\"background-color: #FFCCCC;\"><p><b>ERROR:</b></p>" + fixedHtml + "</div>";
                    break;
                default:
                    ;
            }
            // inject and highlight html message
            browser.execute(
                    """
                            var element = document.getElementById("%s");
                            element.innerHTML = '%s';
                            hljs.highlightAll();"""
                            .formatted("message-" + messageId.toString(), fixedHtml)
            );
            // Scroll down
            browser.execute("window.scrollTo(0, document.body.scrollHeight);");
        });
    }

    @Override
    public void closeElement(Browser browser, UUID messageId) {
        // TODO Auto-generated method stub

    }

}
