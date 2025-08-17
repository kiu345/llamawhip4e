package com.github.kiu345.eclipse.eclipseai.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.Browser;

import com.github.kiu345.eclipse.eclipseai.messaging.Msg.Source;

/**
 * Browser scripting utilities
 */
public class BrowserScripting {
    public static class ScriptException extends RuntimeException {
        private static final long serialVersionUID = 6690740338626392512L;

        public ScriptException(String error) {
            super(error);
        }
    }

    private BrowserScripting() {
    }

    public static final String INPUT_HTML = """
            <span id="edit_area">
                <div id="suggestions" class="chat-bubble"></div>
                <div id="inputarea" class="chat-bubble me current" contenteditable="plaintext-only" autofocus placeholder="Ask anything, '/' for slash commands"></div>
                <div class="context"><div class="header">Context</div><ul class="attachments file-list"></ul></div>
            </span>""";

    public static final String MESSAGE_HTML = """
            <div id="${id}" class="${cssClass}">${message}</div>
            """;

    public static final String BASE_HTML = """
            <html>
            <style>${css}</style>
            <script>${js}</script>
            <body>
              <div class="theme-vs-min">
                <div id="content">${main}</div>
              </div>
            </body>
            </html>""";

    public static final String MSG_ID_PREFIX = "msg-";

    public static String buildBaseHtml(boolean addInput) {
        String htmlTemplate = BASE_HTML;
        String js = loadJavaScripts();
        String css = loadCss();
        htmlTemplate = htmlTemplate
                .replace("${js}", js)
                .replace("${css}", css)
                .replace("${main}", addInput ? INPUT_HTML : "");
        return htmlTemplate;
    }

    /**
     * Loads the JavaScript files for the HTML component.
     *
     * @return A concatenated string containing the content of the loaded JavaScript
     *         files.
     */
    public static String loadJavaScripts() {
        String[] jsFiles = { "functions.js", "highlight.min.js", "init.js" };
        StringBuilder js = new StringBuilder();
        for (String file : jsFiles) {
            try (InputStream in = FileLocator
                    .toFileURL(URI.create("platform:/plugin/com.github.kiu345.eclipse.plugin.eclipseai.main/js/" + file).toURL())
                    .openStream()) {
                js.append(new String(in.readAllBytes(), StandardCharsets.UTF_8));
                js.append("\n");
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return js.toString();
    }

    /**
     * Loads the CSS files for the HTML component.
     *
     * @return A concatenated string containing the content of the loaded CSS files.
     */
    public static String loadCss() {
        StringBuilder css = new StringBuilder();
//        String[] cssFiles = { "textview.css", "dark.min.css" };
        String[] cssFiles = { "textview.css", "hjthemes.css" };
        for (String file : cssFiles) {
            try (InputStream in = FileLocator
                    .toFileURL(URI.create("platform:/plugin/com.github.kiu345.eclipse.plugin.eclipseai.main/css/" + file).toURL())
                    .openStream()) {
                css.append(new String(in.readAllBytes(), StandardCharsets.UTF_8));
                css.append("\n");
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return css.toString();
    }

    public static String messageIdString(UUID id) {
        return MSG_ID_PREFIX + id.toString();
    }

    public static String messageHtml(UUID id, Source source) {
        return messageHtml(id, source, "");
    }

    public static String messageHtml(UUID id, Source source, String htmlContent) {
        String cssClass = Source.USER == source ? "chat-bubble me" : "chat-bubble you";
        return MESSAGE_HTML
                .replace("${id}", messageIdString(id))
                .replace("${cssClass}", cssClass)
                .replace("${message}", htmlContent);
    }

    public static String fixLineBreaks(String html) {
        return html.replace("\n", "\\n").replace("\r", "");
    }

    public static String escapeHtmlQuotes(String html) {
        return html.replace("\"", "\\\"").replace("'", "\\'");
    }

    public static void replaceElementId(Browser browser, String oldId, String newId) {
        var script = """
                let el = document.getElementById("%s");
                el.id = %s;
                """.formatted(oldId, newId != null ? "'" + newId + "'" : "null");
        runScript(browser, script);
    }

    public static void removeElementById(Browser browser, String id) {
        var script = """
                let el = document.getElementById("%s");
                if (el && el.parentNode) {
                   el.parentNode.removeChild(el);
                 }
                 """.formatted(id);
        runScript(browser, script);
    }

    /**
     * SETS html of a element
     */
    public static void setElementContent(Browser browser, UUID messageId, String html) {
        setElementContent(browser, messageIdString(messageId), html);
    }

    public static void setElementContent(Browser browser, String id, String html) {
        var script = "document.getElementById(\"%s\").innerHTML = '%s';"
                .formatted(id, toJsSaveValue(html));
        runScript(browser, script);
    }

    /**
     * ADDS html to the content of a element
     */
    public static void addElementContent(Browser browser, String id, String html) {
        var script = "document.getElementById(\"%s\").innerHTML += '%s';"
                .formatted(id, toJsSaveValue(html));
        runScript(browser, script);
    }

    /**
     * replace special characters and adding line ending escape
     */
    private static String toJsSaveValue(String input) {
        return input.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\\n");
    }

    public static String browserReadyState(Browser browser) {
        return (String) runScript(browser, "return document.readyState;");
    }

    /**
     * maps SWTException to ScriptException and prevents null script executions
     */
    public static Object runScript(Browser browser, String script) {
        if (StringUtils.isBlank(script)) {
            return null;
        }
        try {
            return browser.evaluate(script, false);
        }
        catch (SWTException ex) {
            throw new ScriptException(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }
}
