package com.github.kiu345.eclipse.eclipseai.prompt;

import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;

/**
 * A utility class for parsing and converting a text prompt to an HTML formatted string.
 */
public class InputParser extends UIParser {
    private static final int DEFAULT_STATE = 0;
    private static final int CODE_BLOCK_STATE = 1;
    private static final int FUNCION_CALL_STATE = 2;
    private static final int TEXT_ATTACHMENT_STATE = 4;

    private int state = DEFAULT_STATE;

    private String codeBlockId = "";

    @Override
    public String parseToHtml(UUID msgUuid, String prompt) {
        state = DEFAULT_STATE;
        var out = new StringBuilder();

        prompt = StringEscapeUtils.escapeHtml4(prompt);

        try (var scanner = new Scanner(prompt)) {
            scanner.useDelimiter("\n");

            var codeBlockPattern = Pattern.compile("^(\\s*)```([aA-zZ]*)$");
            while (scanner.hasNext()) {
                var line = scanner.next();
                var codeBlockMatcher = codeBlockPattern.matcher(line);

                if (codeBlockMatcher.find()) {
                    var indentSize = codeBlockMatcher.group(1).length();
                    var lang = codeBlockMatcher.group(2);
                    handleCodeBlock(out, lang, indentSize);
                }
                else {
                    handleNonCodeBlock(out, line, !scanner.hasNext());
                }
            }
        }
        return out.toString();
    }

    private void handleTextAttachmentStart(StringBuilder out, String line) {
        if ((state & TEXT_ATTACHMENT_STATE) != TEXT_ATTACHMENT_STATE) {
            out.append("""

                    <div class="function-call">
                    <details><summary>""");
            state ^= TEXT_ATTACHMENT_STATE;
        }

    }

    private void handleNonCodeBlock(StringBuilder out, String line, boolean lastLine) {
        if ((state & CODE_BLOCK_STATE) == CODE_BLOCK_STATE) {
            out.append(StringEscapeUtils.escapeHtml4(escapeBackSlashes(line)));
        }
//        else if ((state & TEXT_ATTACHMENT_STATE) == TEXT_ATTACHMENT_STATE) {
//            handleTextAttachmentLine(out, line);
//            return;
//        }
        else {
            out.append(markdown(StringEscapeUtils.escapeHtml4(line)));
        }

        if (lastLine && (state & CODE_BLOCK_STATE) == CODE_BLOCK_STATE) // close opened code blocks
        {
            out.append("</code></pre>\n");
        }
        if (lastLine && (state & FUNCION_CALL_STATE) == FUNCION_CALL_STATE) // close opened code blocks
        {
            out.append("</pre></div>\n");
        }
        else if ((state & CODE_BLOCK_STATE) == CODE_BLOCK_STATE) {
            out.append("\n");
        }
        else if (!lastLine) {
            out.append("<br/>");
        }
    }
/*
    private void handleTextAttachmentLine(StringBuilder out, String line) {
        if (line.startsWith(TATT_FILEPREFIX)) {
            out.append("Context: " + line.substring(TATT_FILEPREFIX.length()) + ", ");
        }
        else if (line.startsWith(TATT_LINESPREFIX)) {
            out.append(line + "</summary>");
        }
        else if (line.startsWith(TATT_CONTENTSTART)) {
            out.append("<pre>\n");
        }
        else if (line.startsWith(TATT_CONTENTEND)) {
            out.append("\n</pre>");
        }
        else if (line.startsWith(TATT_CONTEXTEND)) {
            out.append("\n</details></div>\n");
            state ^= TEXT_ATTACHMENT_STATE;
        }
        else {
            out.append(StringEscapeUtils.escapeHtml4(line) + "<br/>");
        }
    }
*/
    private void handleCodeBlock(StringBuilder out, String lang, int indent) {
        if ((state & CODE_BLOCK_STATE) != CODE_BLOCK_STATE) {
            codeBlockId = UUID.randomUUID().toString();
            out.append(
                    """
                            <pre class="inline" style="margin-left: ${indent}pt;"><code class="inline" lang="${lang}" id="${codeBlockId}">
                    """
                    .replace("${indent}", "" + (indent * 5))
                    .replace("${codeBlockId}", codeBlockId)
                    .replace("${lang}", lang)
            );
            state ^= CODE_BLOCK_STATE;
        }
        else {
            out.append("""
                    </code></pre>
                    """);

            state ^= CODE_BLOCK_STATE;
            codeBlockId = "";
        }
    }
}
