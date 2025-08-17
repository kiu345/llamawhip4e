package com.github.kiu345.eclipse.eclipseai.part.helper;

import java.util.UUID;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.swt.browser.Browser;

import com.github.kiu345.eclipse.eclipseai.model.ChatMessage;
import com.github.kiu345.eclipse.eclipseai.prompt.InputParser;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Creatable
@Singleton
public class UserInputHandler extends UIInputHandler {
    @Inject
    private UISynchronize uiSync;

    @Override
    public void createElement(Browser browser, UUID messageId, String role) {
        uiSync.asyncExec(() -> {
            // inject and highlight html message
            browser.execute(
                    "document.getElementById(\"content\").innerHTML += '"
                            + "<div class=\"chat-bubble me current\" contenteditable=\"plaintext-only\" autofocus placeholder=\"Ask a follow-up\"></div>"
                            + "<div id=\"context\" class=\"context\"><div class=\"header\">Context</div><ul id=\"attachments\" class=\"file-list\"></ul></div>"
                            + "';"
            );
            // Scroll down
            browser.execute("addKeyCapture();");
            browser.execute("window.scrollTo(0, document.body.scrollHeight);");
        });        
    }

    @Override
    public void updateElement(Browser browser, UUID messageId, ChatMessage.Type type, String body) {
        uiSync.asyncExec(() -> {
            InputParser parser = new InputParser();
            // NL to Markdown-NL
            String optimizedText = body.trim().replaceAll("\n", "\\\n");
            String fixedHtml = escapeHtmlQuotes(fixLineBreaks(parser.parseToHtml(messageId, optimizedText)));
            // inject and highlight html message
            browser.execute(
                    "var element = document.getElementById(\"message-" + messageId.toString() + "\");" + "element.innerHTML = '"
                            + fixedHtml + "';" + "hljs.highlightElement(element.querySelector('pre code'));"
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
